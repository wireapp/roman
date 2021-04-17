package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.filters.ServiceTokenAuthorization;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Report;
import com.wire.lithium.server.monitoring.MDCUtils;
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
import java.util.concurrent.ExecutorService;
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
    private final ExecutorService broadcast;

    public BroadcastResource(Jdbi jdbi, Sender sender, ExecutorService broadcast) {
        this.sender = sender;
        this.broadcast = broadcast;

        botsDAO = jdbi.onDemand(BotsDAO.class);
        broadcastDAO = jdbi.onDemand(BroadcastDAO.class);
    }

    @POST
    @ApiOperation(value = "Broadcast message on Wire", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {@ApiResponse(code = 401, message = "Not authenticated", response = Report.class)})
    @Metered
    @ServiceTokenAuthorization
    public Response post(@Context ContainerRequestContext context,
                         @ApiParam @HeaderParam(APP_KEY) String token,
                         @ApiParam @NotNull @Valid IncomingMessage message) {
        try {
            trace(message);

            final UUID providerId = (UUID) context.getProperty(PROVIDER_ID);

            Logger.info("BroadcastResource.post: `%s` provider: %s", message.type, providerId);

            List<UUID> botIds = botsDAO.getBotIds(providerId);

            final UUID broadcastId = UUID.randomUUID();

            for (UUID botId : botIds) {
                send(botId, message, broadcastId, providerId);
            }

            Report ret = new Report();
            ret.broadcastId = broadcastId;
            ret.report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.exception("BroadcastResource.post: %s", e, e.getMessage());
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get latest broadcast report", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Unknown broadcastId", response = Report.class)})
    @Metered
    @ServiceTokenAuthorization
    public Response get(@Context ContainerRequestContext context,
                        @ApiParam @HeaderParam(APP_KEY) String token,
                        @ApiParam @QueryParam("id") UUID broadcastId) {
        try {
            final UUID providerId = (UUID) context.getProperty(PROVIDER_ID);

            if (broadcastId == null) {
                broadcastId = broadcastDAO.getBroadcastId(providerId);
            }

            if (broadcastId == null) {
                return Response.
                        status(404).
                        build();
            }

            MDCUtils.put("broadcastId", broadcastId);
            Logger.info("BroadcastResource.get: broadcast: %s provider: %s", broadcastId, providerId);

            Report ret = new Report();
            ret.broadcastId = broadcastId;
            ret.report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.exception("BroadcastResource.get: %s", e, e.getMessage());
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private void send(UUID botId, IncomingMessage message, UUID broadcastId, UUID providerId) {
        broadcast.submit(() -> {
                    try {
                        final UUID messageId = sender.send(message, botId);
                        if (messageId != null) {
                            broadcastDAO.insert(broadcastId, botId, providerId, messageId, BroadcastDAO.Type.SENT.ordinal());
                        }
                    } catch (MissingStateException e) {
                        Logger.warning("BroadcastResource.send %s", e.getMessage());
                        botsDAO.remove(botId);
                    } catch (Exception e) {
                        Logger.exception("BroadcastResource.send %s", e, e.getMessage());
                    }
                }
        );
    }

    private void trace(IncomingMessage message) throws JsonProcessingException {
        if (Logger.getLevel() == Level.FINE) {
            ObjectMapper objectMapper = new ObjectMapper();
            Logger.debug(objectMapper.writeValueAsString(message));
        }
    }
}
