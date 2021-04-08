package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.wire.bots.roman.filters.ProxyAuthorization;
import com.wire.lithium.ClientRepo;
import com.wire.lithium.server.monitoring.MDCUtils;
import com.wire.xenon.WireClient;
import com.wire.xenon.backend.models.ErrorMessage;
import com.wire.xenon.backend.models.User;
import com.wire.xenon.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Api
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    private final ClientRepo repo;

    public UsersResource(ClientRepo repo) {
        this.repo = repo;
    }

    @GET
    @Path("/{userId}")
    @ApiOperation(value = "Get user profile", authorizations = {@Authorization("Bearer")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = User.class, message = "User"),
            @ApiResponse(code = 403, message = "Not authenticated"),
            @ApiResponse(code = 409, message = "Unknown bot. This bot might have been deleted by the user")
    })
    @ProxyAuthorization
    @Metered
    public Response get(@Context ContainerRequestContext context,
                         @ApiParam @PathParam("userId") UUID userId) {
        try {
            final UUID botId = (UUID) context.getProperty("botid");
            MDCUtils.put("botId", botId);
            MDCUtils.put("userId", userId);

            try (WireClient client = repo.getClient(botId)) {
                return Response
                        .ok(client.getUser(userId))
                        .build();
            }
        } catch (Exception e) {
            Logger.exception("UsersResource: %s", e, e.getMessage());
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}
