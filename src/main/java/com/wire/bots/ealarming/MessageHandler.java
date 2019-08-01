package com.wire.bots.ealarming;

import com.wire.bots.ealarming.DAO.User2BotDAO;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.models.TextMessage;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.model.SystemMessage;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import java.util.UUID;

public class MessageHandler extends MessageHandlerBase {
    private final User2BotDAO user2BotDAO;

    MessageHandler(DBI jdbi) {
        user2BotDAO = jdbi.onDemand(User2BotDAO.class);
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

    }
}
