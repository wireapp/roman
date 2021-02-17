package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.filters.ServiceTokenAuthorization;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.xenon.backend.models.ErrorMessage;
import com.wire.xenon.exceptions.MissingStateException;
import com.wire.xenon.tools.Logger;
import io.swagger.annotations.*;
import org.jdbi.v3.core.Jdbi;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
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
    private final Sender sender;
    private final BotsDAO botsDAO;
    private final BroadcastDAO broadcastDAO;

    public BroadcastResource(Jdbi jdbi, Sender sender) {
        this.sender = sender;

        botsDAO = jdbi.onDemand(BotsDAO.class);
        broadcastDAO = jdbi.onDemand(BroadcastDAO.class);
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

            Logger.info("BroadcastResource.post: `%s` provider: %s", message.type, providerId);

            List<UUID> botIds = botsDAO.getBotIds(providerId);

            final UUID broadcastId = UUID.randomUUID();

            int ret = 0;
            for (UUID botId : botIds) {
                if (send(broadcastId, providerId, botId, message))
                    ret++;
            }

            return Response.
                    ok(new ErrorMessage(String.format("%d messages sent", ret))).
                    build();
        } catch (Exception e) {
            Logger.error("BroadcastResource.post: %s", e);
            e.printStackTrace();
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get latest broadcast report", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Not authenticated")})
    @Metered
    @ServiceTokenAuthorization
    public Response get(@Context ContainerRequestContext context,
                        @ApiParam @HeaderParam(APP_KEY) String token) {
        try {
            final UUID providerId = (UUID) context.getProperty(PROVIDER_ID);

            final UUID broadcastId = broadcastDAO.getBroadcastId(providerId);
            if (broadcastId == null) {
                return Response.
                        status(404).
                        build();
            }

            Logger.info("BroadcastResource.get: broadcast: %s provider: %s", broadcastId, providerId);

            final List<BroadcastDAO.Pair> report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(report).
                    build();
        } catch (Exception e) {
            Logger.error("BroadcastResource.get: %s", e);
            e.printStackTrace();
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private boolean send(UUID broadcastId, UUID providerId, UUID botId, IncomingMessage message) {
        try {
            final UUID messageId = sender.send(message, botId);
            if (messageId != null) {
                broadcastDAO.insert(broadcastId, botId, providerId, messageId, BroadcastDAO.Type.SENT.ordinal());
            }
            return messageId != null;
        } catch (MissingStateException e) {
            Logger.warning("BroadcastResource.send: bot: %s, e: %s", botId, e);
            botsDAO.remove(botId);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.warning("BroadcastResource.send: bot: %s, e: %s", botId, e);
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