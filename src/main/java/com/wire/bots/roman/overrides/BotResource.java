package com.wire.bots.roman.resources;

import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.MessageHandler;
import com.wire.bots.sdk.factories.CryptoFactory;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.resources.BotsResource;
import io.swagger.annotations.Api;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/bots")
public class BotResource extends BotsResource {
    private final ProvidersDAO providersDAO;
    private final BotsDAO botsDAO;

    public BotResource(MessageHandler handler, StorageFactory storageF, CryptoFactory cryptoF, DBI jdbi) {
        super(handler, storageF, cryptoF, null);
        this.providersDAO = jdbi.onDemand(ProvidersDAO.class);
        this.botsDAO = jdbi.onDemand(BotsDAO.class);
    }

    @Override
    protected boolean onNewBot(NewBot newBot, String auth) {
        String token = extractToken(auth);
        String url = providersDAO.getUrl(token);
        botsDAO.insert(newBot.id, url, newBot.token);

        return handler.onNewBot(newBot);
    }

    @Override
    protected boolean isValid(String auth) {
        String token = extractToken(auth);
        String url = providersDAO.getUrl(token);
        return url != null;
    }

    private String extractToken(String auth) {
        return auth.replace("Bearer", "").trim();
    }
}
