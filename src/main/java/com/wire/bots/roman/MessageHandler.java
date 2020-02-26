package com.wire.bots.roman;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waz.model.Messages;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.bots.roman.model.Poll;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.DeliveryReceipt;
import com.wire.bots.sdk.models.ImageMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import javax.websocket.EncodeException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.wire.bots.roman.Tools.generateToken;

public class MessageHandler extends MessageHandlerBase {

    private final Client jerseyClient;
    private final ProvidersDAO providersDAO;
    private final BotsDAO botsDAO;

    MessageHandler(DBI jdbi, Client jerseyClient) {
        this.jerseyClient = jerseyClient;
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
        botsDAO = jdbi.onDemand(BotsDAO.class);
    }

    @Override
    public boolean onNewBot(NewBot newBot, String auth) {
        Provider provider = getProvider(auth);
        botsDAO.insert(newBot.id, provider.id);

        UUID botId = newBot.id;
        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = newBot.origin.id;
        message.handle = newBot.origin.handle;
        message.locale = newBot.locale;

        message.type = "conversation.bot_request";

        return send(message);
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        UUID botId = client.getId();

        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = msg.from;
        message.messageId = msg.id;
        message.type = "conversation.init";
        message.text = msg.conversation.name;
        message.token = generateToken(botId);

        boolean send = send(message);
        if (!send)
            Logger.warning("onNewConversation: failed to deliver message to: bot: %s", botId);
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        UUID botId = client.getId();

        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.userId = msg.getUserId();
        message.messageId = msg.getMessageId();
        message.type = "conversation.new_text";
        message.text = msg.getText();
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(30));

        if (send(message)) {
            sendDeliveryReceipt(client, msg.getMessageId(), msg.getUserId());
        } else {
            Logger.warning("onText: failed to deliver message to bot: %s", botId);
        }
    }

    @Override
    public void onImage(WireClient client, ImageMessage msg) {
        UUID botId = client.getId();

        validate(botId);

        try {
            byte[] img = client.downloadAsset(msg.getAssetKey(),
                    msg.getAssetToken(),
                    msg.getSha256(),
                    msg.getOtrKey());

            OutgoingMessage message = new OutgoingMessage();
            message.botId = botId;
            message.userId = msg.getUserId();
            message.messageId = msg.getMessageId();
            message.type = "conversation.new_image";
            message.image = Base64.getEncoder().encodeToString(img);
            message.token = generateToken(botId);

            if (send(message)) {
                sendDeliveryReceipt(client, msg.getMessageId(), msg.getUserId());
            } else {
                Logger.warning("onImage: failed to deliver message to bot: %s", botId);
            }
        } catch (Exception e) {
            Logger.error("onImage: %s %s", botId, e);
        }
    }

    @Override
    public void onEvent(WireClient client, UUID userId, Messages.GenericMessage event) {
        UUID botId = client.getId();
        UUID messageId = UUID.fromString(event.getMessageId());

        if (event.hasButtonAction()) {
            Messages.ButtonAction action = event.getButtonAction();

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
    }

    @Override
    public void onMemberJoin(WireClient client, SystemMessage msg) {
        UUID botId = client.getId();
        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.type = "conversation.user_joined";
        message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(30));

        for (UUID userId : msg.users) {
            try {
                User user = client.getUser(userId);

                message.userId = userId;
                message.handle = user.handle;
                send(message);
            } catch (Exception e) {
                Logger.error("onMemberJoin: %s %s", botId, e);
            }
        }
    }

    @Override
    public void onBotRemoved(UUID botId, SystemMessage msg) {
        validate(botId);

        OutgoingMessage message = new OutgoingMessage();
        message.botId = botId;
        message.type = "conversation.bot_removed";
        boolean send = send(message);
        if (!send)
            Logger.warning("onBotRemoved: failed to deliver message to: bot: %s", botId);

        botsDAO.remove(botId);
    }

    private boolean send(OutgoingMessage message) {
        UUID providerId = botsDAO.getProviderId(message.botId);
        Provider provider = providersDAO.get(providerId);
        if (provider == null) {
            Logger.error("MessageHandler.send: provider == null. providerId: %s, bot: %s",
                    providerId, message.botId);
            return false;
        }

        trace(message);

        // Webhook
        if (provider.serviceUrl != null) {
            Response post = jerseyClient.target(provider.serviceUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + provider.serviceAuth)
                    .post(Entity.entity(message, MediaType.APPLICATION_JSON));

            Logger.info("MessageHandler.send: `%s` bot: %s, provider: %s, status: %d",
                    message.type,
                    message.botId,
                    providerId,
                    post.getStatus());

            return post.getStatus() == 200;
        }

        try {
            return WebSocket.send(provider.id, message);
        } catch (IOException | EncodeException e) {
            Logger.error("MessageHandler.send: bot: %s, provider: %s,  error %s", message.botId, providerId, e);
            return false;
        }
    }

    private void sendDeliveryReceipt(WireClient client, UUID messageId, UUID userId) {
        try {
            client.sendDirectPicture(new DeliveryReceipt(messageId), userId);
        } catch (Exception e) {
            Logger.error("sendDeliveryReceipt: failed to deliver the receipt for message: %s, bot: %s",
                    e,
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
}