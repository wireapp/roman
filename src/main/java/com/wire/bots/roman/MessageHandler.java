package com.wire.bots.ealarming;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.User2BotDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.server.model.User;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

public class MessageHandler extends MessageHandlerBase {
    private final User2BotDAO user2BotDAO;
    private final Alert2UserDAO alert2UserDAO;
    private final UserDAO userDAO;

    MessageHandler(DBI jdbi) {
        user2BotDAO = jdbi.onDemand(User2BotDAO.class);
        alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
        userDAO = jdbi.onDemand(UserDAO.class);
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        UUID botId = newBot.id;
        User origin = newBot.origin;
        UUID userId = origin.id;

        Logger.info(String.format("onNewBot: bot: %s, user: %s, %s",
                botId,
                userId,
                origin.handle));

        return true;
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        UUID botId = client.getId();
        UUID convId = client.getConversationId();
        try {
            client.sendText("This is eAlarming bot");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        UUID userId = msg.getUserId();
        UUID messageId = msg.getQuotedMessageId();
        String text = msg.getText().trim();

    }
}
