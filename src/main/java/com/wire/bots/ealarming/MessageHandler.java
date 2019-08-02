package com.wire.bots.ealarming;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.User2BotDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.ConfirmationMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import java.util.Random;
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
        UUID userId = newBot.origin.id;
        Logger.info(String.format("onNewBot: bot: %s, user: %s",
                newBot.id,
                userId));

        UUID botId = UUID.fromString(newBot.id);
        user2BotDAO.insert(userId, botId);
        try {
            String title = title();
            String department = department();
            String location = location();

            userDAO.insertUser(userId, newBot.origin.name, "", title, department, location);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return true;
    }

    private String location() {
        Random random = new Random();
        String[] ret = new String[]{"Prague", "Berlin", "Zagreb", "Belgrade"};
        return ret[random.nextInt(ret.length)];
    }

    private String department() {
        Random random = new Random();
        String[] ret = new String[]{"Joy Division", "Lost & Found", "Data Science", "Planning, Policy & Management"};
        return ret[random.nextInt(ret.length)];
    }

    private String title() {
        Random random = new Random();
        String[] ret = new String[]{"Officer for moral", "Gunnery sergeant", "Office assistant", "Principal Skinner"};
        return ret[random.nextInt(ret.length)];
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        UUID botId = UUID.fromString(client.getId());
        UUID convId = client.getConversationId();
    }

    @Override
    public void onBotRemoved(UUID botId, SystemMessage msg) {
        user2BotDAO.delete(botId);          //todo delete by userId
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        UUID userId = msg.getUserId();
        UUID messageId = msg.getQuotedMessageId();
        String text = msg.getText().trim();
        if (messageId != null) {
            alert2UserDAO.updateStatus(userId, messageId, text, 4); //todo enum
        }
    }

    public void onConfirmation(WireClient client, ConfirmationMessage msg) {
        UUID userId = msg.getUserId();
        UUID messageId = msg.getConfirmationMessageId();

        switch (msg.getType()) {
            case DELIVERED:
                alert2UserDAO.updateStatus(userId, messageId, null, 2); //todo enum
                break;
            case READ:
                alert2UserDAO.updateStatus(userId, messageId, null, 3); //todo enum
                break;
        }
    }
}
