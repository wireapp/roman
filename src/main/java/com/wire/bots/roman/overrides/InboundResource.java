package com.wire.bots.roman.overrides;


import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.server.model.Payload;
import com.wire.bots.sdk.server.resources.MessageResource;
import io.swagger.annotations.Authorization;

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

    public InboundResource(MessageHandlerBase handler, ClientRepo repo) {
        super(handler, repo);
    }

    @POST
    @Override
    @Authorization("Bearer")
    public Response newMessage(@HeaderParam("Authorization") @NotNull String auth,
                               @PathParam("bot") UUID botId,
                               @QueryParam("id") UUID messageID,
                               @Valid @NotNull Payload payload) throws IOException {

        return super.newMessage(auth, botId, messageID, payload);
    }
}
