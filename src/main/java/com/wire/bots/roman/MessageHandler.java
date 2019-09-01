package com.wire.bots.roman;

import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.model.MessageIn;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
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
            message.convId = newBot.conversation.id;
            message.from = newBot.origin.id;
            message.type = "conversation.bot_added";
            message.token = generateToken(botId);

            String url = botsDAO.getUrl(botId);
            String auth = botsDAO.getToken(botId);

            Response post = jerseyClient.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + auth)
                    .post(Entity.entity(message, MediaType.APPLICATION_JSON));

            Logger.info("onNewBot: %s code: %d", url, post.getStatus());

            return post.getStatus() == 200;
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
            message.convId = client.getConversationId();
            message.from = msg.getUserId();
            message.type = "conversation.new_text";
            message.text = msg.getText();
            message.token = generateToken(botId, TimeUnit.SECONDS.toMillis(30));

            String token = botsDAO.getToken(botId);
            String url = botsDAO.getUrl(botId);

            Response post = jerseyClient.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.entity(message, MediaType.APPLICATION_JSON));

            Logger.info("onText: %s code: %d", url, post.getStatus());

        } catch (Exception e) {
            Logger.error("onText: %s, err: %s", botId, e);
        }
    }
}