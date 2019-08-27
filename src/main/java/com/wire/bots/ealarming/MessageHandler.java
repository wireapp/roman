package com.wire.bots.ealarming;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.User2BotDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.ConfirmationMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.server.model.User;
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
        UUID botId = newBot.id;
        User origin = newBot.origin;
        UUID userId = origin.id;

        Logger.info(String.format("onNewBot: bot: %s, user: %s, %s",
                botId,
                userId,
                origin.handle));

        user2BotDAO.insert(userId, botId);
        try {
            String title = title();
            String department = department();
            String location = location();

            userDAO.insertUser(userId, origin.name, origin.handle, title, department, location);
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
        UUID botId = client.getId();
        UUID convId = client.getConversationId();
        try {
            client.sendText("This is eAlarming bot");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBotRemoved(UUID botId, SystemMessage msg) {
        user2BotDAO.delete(botId);          //todo delete by userId
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        if (msg.getQuotedMessageId() == null)
            return;

        UUID userId = msg.getUserId();
        UUID messageId = msg.getQuotedMessageId();
        String text = msg.getText().trim();
        Alert2User.Type status = Alert2User.Type.RESPONDED;

        int alertId = alert2UserDAO.getAlertId(messageId);
        int update = alert2UserDAO.insertStatus(alertId, userId, status.ordinal(), messageId, text);
        if (update == 0)
            Logger.warning("onConfirmation: user: %s, msgId: %s, %s. update: %s",
                    userId, messageId, status, update);
    }

    public void onConfirmation(WireClient client, ConfirmationMessage msg) {
        UUID userId = msg.getUserId();
        UUID messageId = msg.getConfirmationMessageId();
        Alert2User.Type status = getType(msg.getType());

        int alertId = alert2UserDAO.getAlertId(messageId);
        int update = alert2UserDAO.insertStatus(alertId, userId, status.ordinal(), messageId, null);
        if (update == 0)
            Logger.warning("onConfirmation: user: %s, msgId: %s, %s. update: %s", userId, messageId, status, update);
    }

    private Alert2User.Type getType(ConfirmationMessage.Type type) {
        Alert2User.Type newStatus;
        switch (type) {
            case DELIVERED:
                newStatus = Alert2User.Type.DELIVERED;
                break;
            case READ:
                newStatus = Alert2User.Type.READ;
                break;
            default:
                newStatus = Alert2User.Type.SCHEDULED;
        }
        return newStatus;
    }
}
