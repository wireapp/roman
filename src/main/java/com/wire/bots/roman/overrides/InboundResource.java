package com.wire.bots.roman.overrides;


import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.server.model.Payload;
import com.wire.bots.sdk.server.resources.MessageResource;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/bots/{bot}/messages")
public class InboundResource extends MessageResource {
    private final DBI jdbi;

    public InboundResource(MessageHandlerBase handler, ClientRepo repo, DBI jdbi) {
        super(handler, null, repo);
        this.jdbi = jdbi;
    }

    @POST
    @Override
    public Response newMessage(@HeaderParam("Authorization") @NotNull String auth,
                               @PathParam("bot") UUID botId,
                               @QueryParam("id") UUID messageID,
                               @Valid @NotNull Payload payload) throws IOException {

        return super.newMessage(auth, botId, messageID, payload);
    }

    protected boolean isValid(String auth) {
        auth = auth.replace("Bearer", "").trim();
        String url = jdbi.onDemand(ProvidersDAO.class).getUrl(auth);
        return url != null;
    }
}
