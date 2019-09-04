package com.wire.bots.roman.overrides;

import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.factories.CryptoFactory;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.server.model.NewBot;
import com.wire.bots.sdk.server.resources.BotsResource;
import com.wire.bots.sdk.tools.Logger;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/bots")
public class BotResource extends BotsResource {
    private final DBI jdbi;

    public BotResource(MessageHandlerBase handler, StorageFactory storageF, CryptoFactory cryptoF, DBI jdbi) {
        super(handler, storageF, cryptoF, null);
        this.jdbi = jdbi;
    }

    @POST
    @Override
    public Response newBot(@HeaderParam("Authorization") @NotNull String auth,
                           @Valid @NotNull NewBot newBot) throws Exception {

        return super.newBot(auth, newBot);
    }

    @Override
    protected boolean onNewBot(NewBot newBot, String auth) {
        auth = stripType(auth);
        String url = jdbi.onDemand(ProvidersDAO.class).getUrl(auth);
        int insert = jdbi.onDemand(BotsDAO.class).insert(newBot.id, url, auth);
        if (insert == 0) {
            Logger.error("Failed to insert `url` and `auth` to Bots table");
            return false;
        }
        return handler.onNewBot(newBot);
    }

    @Override
    protected boolean isValid(String auth) {
        auth = stripType(auth);
        String url = jdbi.onDemand(ProvidersDAO.class).getUrl(auth);
        return url != null;
    }

    private String stripType(String auth) {
        return auth.replace("Bearer", "").trim();
    }
}
