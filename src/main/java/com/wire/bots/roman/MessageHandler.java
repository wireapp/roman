package com.wire.bots.roman;

import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.model.MessageIn;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.wire.bots.roman.Tools.generateToken;

public class MessageHandler extends MessageHandlerBase {
    private final BotsDAO botsDAO;

    private final Client jerseyClient;

    MessageHandler(DBI jdbi, Client jerseyClient) {
        this.botsDAO = jdbi.onDemand(BotsDAO.class);
        this.jerseyClient = jerseyClient;
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        UUID botId = newBot.id;
        try {
            MessageIn message = new MessageIn();
            message.botId = botId;
            message.from = newBot.origin.id;
            message.type = "conversation.bot_request";

            return send(message);
        } catch (Exception e) {
            Logger.error("onNewBot: %s, err: %s", botId, e);
            return false;
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        UUID botId = client.getId();
        try {
            MessageIn message = new MessageIn();
            message.botId = botId;
            message.from = msg.getUserId();
            message.type = "conversation.new_text";
            message.text = msg.getText();
            message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(30));

            send(message);

        } catch (Exception e) {
            Logger.error("onText: %s, err: %s", botId, e);
        }
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        UUID botId = client.getId();
        try {
            MessageIn message = new MessageIn();
            message.botId = botId;
            message.from = msg.from;
            message.type = "conversation.new_conversation";
            message.token = generateToken(botId);

            send(message);

        } catch (Exception e) {
            Logger.error("onNewConversation: %s, err: %s", botId, e);
        }
    }

    private boolean send(MessageIn message) {
        String token = botsDAO.getServiceAuthorization(message.botId);
        String url = botsDAO.getUrl(message.botId);

        Response post = jerseyClient.target(url)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .post(Entity.entity(message, MediaType.APPLICATION_JSON));

        Logger.info("%s: %s code: %d", message.type, message.botId, post.getStatus());
        return post.getStatus() == 200;
    }
}