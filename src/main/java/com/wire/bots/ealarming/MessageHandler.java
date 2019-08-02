package com.wire.bots.ealarming;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.User2BotDAO;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.ConfirmationMessage;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

public class MessageHandler extends MessageHandlerBase {
    private final User2BotDAO user2BotDAO;
    private final Alert2UserDAO alert2UserDAO;

    MessageHandler(DBI jdbi) {
        user2BotDAO = jdbi.onDemand(User2BotDAO.class);
        alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
    }

    @Override
    public boolean onNewBot(NewBot newBot) {
        Logger.info(String.format("onNewBot: bot: %s, user: %s",
                newBot.id,
                newBot.origin.id));

        user2BotDAO.insert(newBot.origin.id, UUID.fromString(newBot.id));
        return true;
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
