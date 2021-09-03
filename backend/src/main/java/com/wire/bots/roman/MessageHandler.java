package com.wire.bots.roman;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waz.model.Messages;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.DAO.OutgoingMessageDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.*;
import com.wire.lithium.server.monitoring.MDCUtils;
import com.wire.xenon.MessageHandlerBase;
import com.wire.xenon.WireClient;
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
    private final OutgoingMessageDAO outgoingMessageDAO;
    private Sender sender;

    MessageHandler(Jdbi jdbi, Client jerseyClient) {
        this.jerseyClient = jerseyClient;
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
        botsDAO = jdbi.onDemand(BotsDAO.class);
        broadcastDAO = jdbi.onDemand(BroadcastDAO.class);
        outgoingMessageDAO = jdbi.onDemand(OutgoingMessageDAO.class);
    }

    @Override
    public boolean onNewBot(NewBot newBot, String auth) {
        Provider provider = getProvider(auth);
        botsDAO.insert(newBot.id, provider.id);

        UUID botId = newBot.id;
        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.conversationId = newBot.conversation.id;
        message.conversation = newBot.conversation.name;
        message.userId = newBot.origin.id;
        message.handle = newBot.origin.handle;
        message.locale = newBot.locale;
        message.token = generateToken(botId);

        message.type = "conversation.bot_request";

        final boolean delivered = send(message);
        if (!delivered) {
            Logger.info("Failed to connect to: %s or the websocket is down, provider: %s. Rejecting this bot",
                    provider.serviceUrl, provider.id);
            botsDAO.remove(botId);
        }

        return delivered;
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        final UUID botId = client.getId();
        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.type = "conversation.init";
        message.botId = botId;
        message.userId = msg.from;
        message.conversationId = msg.conversation.id;
        message.messageId = msg.id;
        message.conversation = msg.conversation.name;
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(TOKEN_DURATION));

        boolean send = send(message);
        if (!send)
            Logger.warning("onNewConversation: failed to deliver message");
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        final UUID botId = client.getId();
        validate(botId);
        final String type = "conversation.new_text";

        OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
        message.refMessageId = msg.getQuotedMessageId();
        message.text = new Text();
        message.text.data = msg.getText();

        for (TextMessage.Mention mention : msg.getMentions()) {
            Mention m = new Mention();
            m.userId = mention.userId;
            m.offset = mention.offset;
            m.length = mention.length;

            if (message.text.mentions == null)
                message.text.mentions = new ArrayList<>();

            message.text.mentions.add(m);
        }

        send(message);
    }

    @Override
    public void onReaction(WireClient client, ReactionMessage msg) {
        final String type = "conversation.reaction";

        UUID botId = client.getId();

        validate(botId);

        OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
        message.refMessageId = msg.getReactionMessageId();
        message.emoji = msg.getEmoji();

        send(message);
    }

    @Override
    public void onPhotoPreview(WireClient client, PhotoPreviewMessage msg) {
        final String type = "conversation.image.preview";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
            message.attachment = fromOriginal(msg);
            message.attachment.width = msg.getWidth();
            message.attachment.height = msg.getHeight();

            outgoingMessageDAO.insert(message.messageId, mapper.writeValueAsString(message));

            send(message);
        } catch (Exception e) {
            Logger.exception("onPhotoPreview: %s", e, e.getMessage());
        }
    }

    @Override
    public void onFilePreview(WireClient client, FilePreviewMessage msg) {
        final String type = "conversation.file.preview";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
            message.attachment = fromOriginal(msg);

            outgoingMessageDAO.insert(message.messageId, mapper.writeValueAsString(message));

            send(message);
        } catch (Exception e) {
            Logger.exception("onFilePreview: %s", e, e.getMessage());
        }
    }

    @Override
    public void onAudioPreview(WireClient client, AudioPreviewMessage msg) {
        final String type = "conversation.audio.preview";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
            message.attachment = fromOriginal(msg);
            message.attachment.duration = msg.getDuration();
            message.attachment.levels = msg.getLevels();

            outgoingMessageDAO.insert(message.messageId, mapper.writeValueAsString(message));

            send(message);
        } catch (Exception e) {
            Logger.exception("onAudioPreview: %s", e, e.getMessage());
        }
    }

    @Override
    public void onVideoPreview(WireClient client, VideoPreviewMessage msg) {
        final String type = "conversation.video.preview";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
            message.attachment = fromOriginal(msg);
            message.attachment.width = msg.getWidth();
            message.attachment.height = msg.getHeight();
            message.attachment.duration = msg.getDuration();

            outgoingMessageDAO.insert(message.messageId, mapper.writeValueAsString(message));

            send(message);
        } catch (Exception e) {
            Logger.exception("onVideoPreview: %s", e, e.getMessage());
        }
    }

    @Override
    public void onAssetData(WireClient client, RemoteMessage msg) {
        final String type = "conversation.asset.data";

        UUID botId = client.getId();

        validate(botId);

        try {
            OutgoingMessage message = outgoingMessageDAO.get(msg.getMessageId());
            if (message == null) {
                throw new Exception("No asset preview data for msg: " + msg.getMessageId());
            }

            message.type = type;
            message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(TOKEN_DURATION));
            message.attachment.meta = extractAssetMeta(msg);

            send(message);

            outgoingMessageDAO.delete(msg.getMessageId());
        } catch (Exception e) {
            Logger.exception("onAssetData: %s", e, e.getMessage());
        }
    }

    @Override
    public void onPing(WireClient client, PingMessage msg) {
        final String type = "conversation.ping";

        UUID botId = client.getId();

        validate(botId);

        OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);

        send(message);
    }

    @Override
    public void onEvent(WireClient client, UUID userId, Messages.GenericMessage event) {
        final UUID botId = client.getId();
        final UUID conversationId = client.getConversationId();

        // User clicked on a Poll Button
        if (event.hasButtonAction()) {
            onButtonAction(botId, userId, conversationId, event);
        }
        // New Poll has been created
        if (event.hasComposite()) {
            onComposite(botId, userId, conversationId, event);
        }
    }

    @Override
    public void onConfirmation(WireClient client, ConfirmationMessage msg) {
        try {
            final UUID messageId = msg.getConfirmationMessageId();
            final ConfirmationMessage.Type type = msg.getType();

            broadcastDAO.insertStatus(messageId, type == ConfirmationMessage.Type.DELIVERED ? DELIVERED.ordinal() : READ.ordinal());
        } catch (Exception e) {
            Logger.exception("onConfirmation: %s", e, e.getMessage());
        }
    }

    private void onComposite(UUID botId, UUID userId, UUID conversationId, Messages.GenericMessage event) {
        final String type = "conversation.poll.new";
        final UUID messageId = UUID.fromString(event.getMessageId());

        OutgoingMessage message = createOutgoingMessage(botId, userId, type, conversationId, messageId);

        message.poll = new Poll();
        message.poll.id = messageId;
        message.poll.type = "new";
        message.poll.buttons = new ArrayList<>();

        for (Messages.Composite.Item item : event.getComposite().getItemsList()) {
            if (item.hasText()) {
                message.text = new Text();
                message.text.data = item.getText().getContent();
            }
            if (item.hasButton()) {
                message.poll.buttons.add(item.getButton().getText());
            }
        }

        if (!send(message)) {
            Logger.warning("onComposite: failed to deliver message");
        }
    }

    private void onButtonAction(UUID botId, UUID userId, UUID conversationId, Messages.GenericMessage event) {
        final String type = "conversation.poll.action";

        final UUID messageId = UUID.fromString(event.getMessageId());
        OutgoingMessage message = createOutgoingMessage(botId, userId, type, conversationId, messageId);

        final Messages.ButtonAction action = event.getButtonAction();
        message.poll = new Poll();
        message.poll.id = UUID.fromString(action.getReferenceMessageId());
        message.poll.offset = Integer.parseInt(action.getButtonId());

        if (!send(message)) {
            Logger.warning("onButtonAction: failed to deliver message");
        }
    }

    @Override
    public void onCalling(WireClient client, CallingMessage msg) {
        try {
            final String type = "conversation.call";

            final UUID botId = client.getId();

            validate(botId);

            OutgoingMessage message = createOutgoingMessage(botId, type, client.getConversationId(), msg);
            message.call = mapper.readValue(msg.getContent(), Call.class);

            send(message);
        } catch (Exception e) {
            Logger.exception("onCalling: error: %s", e, e.getMessage());
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
                Logger.exception("onMemberJoin: %s", e, e.getMessage());
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
            Logger.info("onBotRemoved: failed to deliver message");

        botsDAO.remove(botId);
    }

    private boolean send(OutgoingMessage message) {
        final UUID providerId = validate(message.botId);

        try {
            Provider provider = providersDAO.get(providerId);
            if (provider == null) {
                Logger.error("MessageHandler.send: Unknown provider");
                return false;
            }

            trace(message);

            if (shouldIgnore(provider, message)) {
                Logger.debug("MessageHandler.send: Ignoring %s", message.type);
                return true;
            }

            // Webhook
            if (provider.serviceUrl != null) {
                Response post = jerseyClient.target(provider.serviceUrl)
                        .request(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + provider.serviceAuth)
                        .post(Entity.entity(message, MediaType.APPLICATION_JSON));

                Logger.debug("MessageHandler.send: Sent: `%s` status: %d", message.type, post.getStatus());

                if (message.type.equalsIgnoreCase("conversation.bot_request"))
                    return post.getStatus() == 200;

                if (post.hasEntity()) {
                    final IncomingMessage incomingMessage = getIncomingMessage(post);
                    if (incomingMessage != null && incomingMessage.type != null) {
                        Logger.debug("MessageHandler.send: Posting `%s` into conversation.", incomingMessage.type);
                        sender.send(incomingMessage, message.botId);
                    }
                }

                return post.getStatus() == 200;
            } else {
                return WebSocket.send(provider.id, message);
            }
        } catch (Exception e) {
            Logger.exception("MessageHandler.send: error %s", e, e.getMessage());
            return false;
        }
    }

    private boolean shouldIgnore(Provider provider, OutgoingMessage message) {
        if (message.type.equalsIgnoreCase("conversation.bot_request"))
            return false;

        if (provider.commandPrefix == null)
            return false;

        if (provider.commandPrefix.equalsIgnoreCase("***"))
            return true;

        if (!message.type.equalsIgnoreCase("conversation.new_text"))
            return true;

        final String text = message.text.data;

        return !text.startsWith(provider.commandPrefix);
    }

    private IncomingMessage getIncomingMessage(Response post) {
        try {
            return post.readEntity(IncomingMessage.class);
        } catch (ProcessingException e) {
            return null;
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
        MDCUtils.put("providerId", provider.id);
        return provider;
    }

    private UUID validate(UUID botId) {
        UUID providerId = botsDAO.getProviderId(botId);
        if (providerId == null)
            throw new RuntimeException("Unknown botId: " + botId.toString());
        MDCUtils.put("providerId", providerId);
        return providerId;
    }

    private OutgoingMessage createOutgoingMessage(UUID botId, UUID userId, String type, UUID conversationId, UUID messageId) {
        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = userId;
        message.messageId = messageId;
        message.type = type;
        message.conversationId = conversationId;
        message.token = generateToken(botId);
        return message;
    }

    private OutgoingMessage createOutgoingMessage(UUID botId, String type, UUID conversationId, MessageBase msg) {
        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.type = type;
        message.userId = msg.getUserId();
        message.messageId = msg.getMessageId();
        message.conversationId = conversationId;
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(TOKEN_DURATION));
        return message;
    }

    private Attachment fromOriginal(OriginMessage msg) {
        final Attachment attachment = new Attachment();
        attachment.name = msg.getName();
        attachment.mimeType = msg.getMimeType();
        attachment.size = msg.getSize();
        return attachment;
    }

    private AssetMeta extractAssetMeta(RemoteMessage msg) {
        AssetMeta meta = new AssetMeta();
        meta.assetId = msg.getAssetId();
        meta.assetToken = msg.getAssetToken();
        meta.sha256 = Base64.getEncoder().encodeToString(msg.getSha256());
        meta.otrKey = Base64.getEncoder().encodeToString(msg.getOtrKey());
        return meta;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }
}
