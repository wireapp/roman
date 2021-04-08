package com.wire.bots.roman;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waz.model.Messages;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.*;
import com.wire.xenon.MessageHandlerBase;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.DeliveryReceipt;
import com.wire.xenon.backend.models.NewBot;
import com.wire.xenon.backend.models.SystemMessage;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.models.*;
import com.wire.xenon.tools.Logger;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.wire.bots.roman.DAO.BroadcastDAO.Type.DELIVERED;
import static com.wire.bots.roman.DAO.BroadcastDAO.Type.READ;
import static com.wire.bots.roman.Tools.generateToken;

public class MessageHandler extends MessageHandlerBase {

    private static final int TOKEN_DURATION = 20;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Client jerseyClient;
    private final ProvidersDAO providersDAO;
    private final BotsDAO botsDAO;
    private final BroadcastDAO broadcastDAO;
    private Sender sender;

    MessageHandler(Jdbi jdbi, Client jerseyClient) {
        this.jerseyClient = jerseyClient;
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
        botsDAO = jdbi.onDemand(BotsDAO.class);
        broadcastDAO = jdbi.onDemand(BroadcastDAO.class);
    }

    @Override
    public boolean onNewBot(NewBot newBot, String auth) {
        Provider provider = getProvider(auth);
        botsDAO.insert(newBot.id, provider.id);

        UUID botId = newBot.id;
        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.conversationId = newBot.conversation.id;
        message.userId = newBot.origin.id;
        message.handle = newBot.origin.handle;
        message.locale = newBot.locale;
        message.token = generateToken(botId);

        message.type = "conversation.bot_request";

        return send(message);
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        final UUID botId = client.getId();
        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = msg.from;
        message.conversationId = msg.conversation.id;
        message.messageId = msg.id;
        message.type = "conversation.init";
        message.text = msg.conversation.name;
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(TOKEN_DURATION));

        boolean send = send(message);
        if (!send)
            Logger.warning("onNewConversation: failed to deliver message to: bot: %s", botId);
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        final UUID botId = client.getId();
        validate(botId);
        final String type = "conversation.new_text";

        OutgoingMessage message = getOutgoingMessage(botId, type, msg);
        message.conversationId = client.getConversationId();
        message.refMessageId = msg.getQuotedMessageId();
        message.text = msg.getText();
        for (TextMessage.Mention mention : msg.getMentions())
            message.addMention(mention.userId, mention.offset, mention.length);

        send(message);
    }

    @Override
    public void onReaction(WireClient client, ReactionMessage msg) {
        final String type = "conversation.reaction";

        UUID botId = client.getId();

        validate(botId);

        OutgoingMessage message = getOutgoingMessage(botId, type, msg);

        message.refMessageId = msg.getReactionMessageId();
        message.text = msg.getEmoji();
        message.conversationId = client.getConversationId();

        send(message);
    }

    @Override
    public void onImage(WireClient client, ImageMessage msg) {
        final String type = "conversation.new_image";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = getOutgoingMessage(botId, type, msg);

            byte[] img = client.downloadAsset(msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey());
            message.image = Base64.getEncoder().encodeToString(img);
            message.mimeType = msg.getMimeType();
            message.conversationId = client.getConversationId();

            send(message);
        } catch (Exception e) {
            Logger.exception("onImage: %s %s", e, botId, e.getMessage());
        }
    }

    public void onAttachment(WireClient client, AttachmentMessage msg) {
        final String type = "conversation.file.new";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = getOutgoingMessage(botId, type, msg);

            byte[] img = client.downloadAsset(msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey());
            message.attachment = Base64.getEncoder().encodeToString(img);
            message.text = msg.getName();
            message.mimeType = msg.getMimeType();
            message.conversationId = client.getConversationId();

            send(message);
        } catch (Exception e) {
            Logger.exception("onAttachment: %s %s", e, botId, e.getMessage());
        }
    }

    @Override
    public void onAudio(WireClient client, AudioMessage msg) {
        final String type = "conversation.audio.new";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = getOutgoingMessage(botId, type, msg);

            byte[] img = client.downloadAsset(msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey());
            message.attachment = Base64.getEncoder().encodeToString(img);
            message.text = msg.getName();
            message.mimeType = msg.getMimeType();
            message.duration = msg.getDuration();
            message.conversationId = client.getConversationId();

            send(message);
        } catch (Exception e) {
            Logger.exception("onAudio: %s %s", e, botId, e.getMessage());
        }
    }

    @Override
    public void onEvent(WireClient client, UUID userId, Messages.GenericMessage event) {
        final UUID botId = client.getId();

        // User clicked on a Poll Button
        if (event.hasButtonAction()) {
            onButtonAction(botId, userId, event);
        }
        // New Poll has been created
        if (event.hasComposite()) {
            onComposite(botId, userId, event);
        }
    }

    @Override
    public void onConfirmation(WireClient client, ConfirmationMessage msg) {
        try {
            final UUID messageId = msg.getConfirmationMessageId();
            final ConfirmationMessage.Type type = msg.getType();

            broadcastDAO.insertStatus(messageId, type == ConfirmationMessage.Type.DELIVERED ? DELIVERED.ordinal() : READ.ordinal());
        } catch (Exception e) {
            Logger.exception("onConfirmation: %s %s", e, client.getId(), e.getMessage());
        }
    }

    private void onComposite(UUID botId, UUID userId, Messages.GenericMessage event) {
        final Messages.Composite composite = event.getComposite();
        final UUID messageId = UUID.fromString(event.getMessageId());

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = userId;
        message.messageId = messageId;
        message.type = "conversation.poll.new";
        message.token = generateToken(botId);
        message.poll = new Poll();
        message.poll.id = messageId;
        message.poll.type = "new";
        message.poll.buttons = new ArrayList<>();
        for (Messages.Composite.Item item : composite.getItemsList()) {
            if (item.hasText()) {
                message.text = item.getText().getContent();
            }
            if (item.hasButton()) {
                message.poll.buttons.add(item.getButton().getText());
            }
        }

        if (!send(message)) {
            Logger.warning("onEvent: failed to deliver message to bot: %s", botId);
        }
    }

    private void onButtonAction(UUID botId, UUID userId, Messages.GenericMessage event) {
        final Messages.ButtonAction action = event.getButtonAction();
        final UUID messageId = UUID.fromString(event.getMessageId());

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = userId;
        message.messageId = messageId;
        message.type = "conversation.poll.action";
        message.token = generateToken(botId);
        message.poll = new Poll();
        message.poll.id = UUID.fromString(action.getReferenceMessageId());
        message.poll.offset = Integer.parseInt(action.getButtonId());

        if (!send(message)) {
            Logger.warning("onEvent: failed to deliver message to bot: %s", botId);
        }
    }

    @Override
    public void onCalling(WireClient client, CallingMessage msg) {
        try {
            final String type = "conversation.call";

            final UUID botId = client.getId();

            validate(botId);

            OutgoingMessage message = getOutgoingMessage(botId, type, msg);
            message.conversationId = client.getConversationId();
            message.call = mapper.readValue(msg.getContent(), Call.class);

            send(message);
        } catch (Exception e) {
            Logger.exception("onCalling: bot: %s error: %s", e, client.getId(), e.getMessage());
        }
    }

    @Override
    public void onMemberJoin(WireClient client, SystemMessage msg) {
        final UUID botId = client.getId();
        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.type = "conversation.user_joined";
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(TOKEN_DURATION));

        for (UUID userId : msg.users) {
            try {
                User user = client.getUser(userId);

                message.userId = userId;
                message.handle = user.handle;
                send(message);
            } catch (Exception e) {
                Logger.exception("onMemberJoin: %s %s", e, botId, e.getMessage());
            }
        }
    }

    @Override
    public void onBotRemoved(UUID botId, SystemMessage msg) {
        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.type = "conversation.bot_removed";

        if (!send(message))
            Logger.warning("onBotRemoved: failed to deliver message to: bot: %s", botId);

        botsDAO.remove(botId);
    }

    private OutgoingMessage getOutgoingMessage(UUID botId, String type, MessageBase msg) {
        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.type = type;
        message.userId = msg.getUserId();
        message.messageId = msg.getMessageId();
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(TOKEN_DURATION));
        return message;
    }

    private boolean send(OutgoingMessage message) {
        final UUID providerId = botsDAO.getProviderId(message.botId);

        try {
            Provider provider = providersDAO.get(providerId);
            if (provider == null) {
                Logger.error("MessageHandler.send: provider == null. providerId: %s", providerId);
                return false;
            }

            trace(message);

            // Webhook
            if (provider.serviceUrl != null) {
                Response post = jerseyClient.target(provider.serviceUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + provider.serviceAuth)
                        .post(Entity.entity(message, MediaType.APPLICATION_JSON));

                Logger.debug("MessageHandler.send: Sent: `%s` provider: %s, status: %d",
                        message.type,
                        providerId,
                        post.getStatus());

                if (post.hasEntity()) {
                    final IncomingMessage incomingMessage = getIncomingMessage(post);
                    if (incomingMessage != null && incomingMessage.type != null) {
                        Logger.debug("MessageHandler.send: Posting `%s` into conversation. Provider: %s",
                                incomingMessage.type,
                                providerId
                        );
                        sender.send(incomingMessage, message.botId);
                    }
                }

                return post.getStatus() == 200;
            } else {
                return WebSocket.send(provider.id, message);
            }
        } catch (Exception e) {
            Logger.exception("MessageHandler.send: bot: %s, provider: %s,  error %s", e, message.botId, providerId, e.getMessage());
            return false;
        }
    }

    private IncomingMessage getIncomingMessage(Response post) {
        try {
            return post.readEntity(IncomingMessage.class);
        } catch (ProcessingException e) {
            return null;
        }
    }

    private void sendDeliveryReceipt(WireClient client, UUID messageId, UUID userId) {
        try {
            client.send(new DeliveryReceipt(messageId), userId);
        } catch (Exception e) {
            Logger.exception("sendDeliveryReceipt: failed to deliver the receipt for message: %s, bot: %s",
                    e,
                    e.getMessage(),
                    client.getId());
        }
    }

    private void trace(OutgoingMessage message) {
        try {
            if (Logger.getLevel() == Level.FINE) {
                ObjectMapper objectMapper = new ObjectMapper();
                Logger.debug(objectMapper.writeValueAsString(message));
            }
        } catch (Exception ignore) {

        }
    }

    private Provider getProvider(String auth) {
        final Provider provider = providersDAO.getByAuth(auth);
        if (provider == null)
            throw new RuntimeException("Unknown auth");
        return provider;
    }

    private UUID validate(UUID botId) {
        UUID providerId = botsDAO.getProviderId(botId);
        if (providerId == null)
            throw new RuntimeException("Unknown botId: " + botId.toString());
        return providerId;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}
