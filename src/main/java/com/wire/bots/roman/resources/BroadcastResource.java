package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.filters.ServiceTokenAuthorization;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Provider;
import com.wire.xenon.backend.models.ErrorMessage;
import com.wire.xenon.exceptions.MissingStateException;
import com.wire.xenon.tools.Logger;
import io.jsonwebtoken.JwtException;
import io.swagger.annotations.*;
import org.jdbi.v3.core.Jdbi;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static com.wire.bots.roman.Const.APP_KEY;
import static com.wire.bots.roman.Const.PROVIDER_ID;

@Api
@Path("/broadcast")
@Produces(MediaType.APPLICATION_JSON)
public class BroadcastResource {
    private final Jdbi jdbi;
    private final Sender sender;

    public BroadcastResource(Jdbi jdbi, Sender sender) {
        this.jdbi = jdbi;
        this.sender = sender;
    }

    @POST
    @ApiOperation(value = "Broadcast message on Wire", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Not authenticated"),
            @ApiResponse(code = 404, message = "Unknown access token")
    })
    @Metered
    @ServiceTokenAuthorization
    public Response post(@Context ContainerRequestContext context,
                         @ApiParam @HeaderParam(APP_KEY) String token,
                         @ApiParam @NotNull @Valid IncomingMessage message) {
        try {
            trace(message);

            UUID providerId = (UUID) context.getProperty(PROVIDER_ID);

            ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);
            Provider provider = providersDAO.get(providerId);
            if (provider == null) {
                return Response.
                        ok(new ErrorMessage("Unknown access token")).
                        status(404).
                        build();
            }

            Logger.info("BroadcastResource: `%s` provider: %s", message.type, providerId);

            BotsDAO botsDAO = jdbi.onDemand(BotsDAO.class);

            List<UUID> botIds = botsDAO.getBotIds(providerId);

            int ret = 0;
            for (UUID botId : botIds) {
                if (send(botId, message))
                    ret++;
            }

            return Response.
                    ok(new ErrorMessage(String.format("%d messages sent", ret))).
                    build();
        } catch (JwtException e) {
            Logger.warning("BroadcastResource %s", e);
            return Response.
                    ok(new ErrorMessage("Invalid Authorization token")).
                    status(403).
                    build();
        } catch (Exception e) {
            Logger.error("BroadcastResource: %s", e);
            e.printStackTrace();
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private boolean send(UUID botId, IncomingMessage message) {
        try {
            final UUID messageId = sender.send(message, botId);
            return messageId != null;
        } catch (MissingStateException e) {
            Logger.warning("BroadcastResource: bot: %s, e: %s", botId, e);
            jdbi.onDemand(BotsDAO.class).remove(botId);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.warning("BroadcastResource: bot: %s, e: %s", botId, e);
        }

        return false;
    }

    private void trace(IncomingMessage message) {
        try {
            if (Logger.getLevel() == Level.FINE) {
                ObjectMapper objectMapper = new ObjectMapper();
                Logger.debug(objectMapper.writeValueAsString(message));
            }
        } catch (Exception ignore) {

        }
    }
}