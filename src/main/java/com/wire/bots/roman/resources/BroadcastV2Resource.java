package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.filters.ServiceTokenAuthorization;
import com.wire.bots.roman.model.BroadcastMessage;
import com.wire.bots.roman.model.Report;
import com.wire.lithium.server.monitoring.MDCUtils;
import com.wire.xenon.assets.*;
import com.wire.xenon.backend.models.ErrorMessage;
import com.wire.xenon.tools.Logger;
import io.swagger.annotations.*;
import org.jdbi.v3.core.Jdbi;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import static com.wire.bots.roman.Const.APP_KEY;
import static com.wire.bots.roman.Const.PROVIDER_ID;

@Api
@Path("/broadcast/v2")
@Produces(MediaType.APPLICATION_JSON)
public class BroadcastV2Resource {
    private final Sender sender;
    private final BotsDAO botsDAO;
    private final BroadcastDAO broadcastDAO;
    private final ExecutorService broadcast;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BroadcastV2Resource(Jdbi jdbi, Sender sender, ExecutorService broadcast) {
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
                         @ApiParam @NotNull @Valid BroadcastMessage message) {
        try {
            trace(message);

            final UUID providerId = (UUID) context.getProperty(PROVIDER_ID);

            List<UUID> botIds = botsDAO.getBotIds(providerId);

            final UUID broadcastId = UUID.randomUUID();

            MDCUtils.put("broadcastId", broadcastId);
            Logger.info("BroadcastV2Resource.post: `%s`", message.mimeType);

            for (UUID botId : botIds) {
                broadcast.submit(() -> {
                    if (message.mimeType.startsWith("audio")) {
                        sendAudio(message, providerId, broadcastId, botId);
                    } else if (message.mimeType.startsWith("image")) {
                        sendPicture(message, providerId, broadcastId, botId);
                    } else {
                        sendFile(message, providerId, broadcastId, botId);
                    }
                });
            }

            Report ret = new Report();
            ret.broadcastId = broadcastId;
            ret.report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.exception("BroadcastV2Resource.post", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private void sendAudio(BroadcastMessage message, UUID providerId, UUID broadcastId, UUID botId) {
        final AudioPreview preview = new AudioPreview(
                message.filename,
                message.mimeType,
                message.duration,
                message.levels,
                message.size);

        final AudioAsset audioAsset = new AudioAsset(preview.getMessageId(), message.mimeType);
        audioAsset.setAssetKey(message.assetKey);
        audioAsset.setAssetToken(message.assetToken);
        audioAsset.setSha256(Base64.getDecoder().decode(message.sha256));
        audioAsset.setOtrKey(Base64.getDecoder().decode(message.otrKey));

        sendAsset(providerId, broadcastId, preview, audioAsset, botId);
    }

    private void sendFile(BroadcastMessage message, UUID providerId, UUID broadcastId, UUID botId) {
        final FileAssetPreview preview = new FileAssetPreview(
                message.filename,
                message.mimeType,
                message.size,
                UUID.randomUUID());

        final FileAsset audioAsset = new FileAsset(message.assetKey,
                message.assetToken,
                Base64.getDecoder().decode(message.sha256),
                Base64.getDecoder().decode(message.otrKey),
                preview.getMessageId());

        sendAsset(providerId, broadcastId, preview, audioAsset, botId);
    }

    private void sendPicture(BroadcastMessage message, UUID providerId, UUID broadcastId, UUID botId) {
        final Picture picture = new Picture(UUID.randomUUID(), message.mimeType);
        picture.setAssetKey(message.assetKey);
        picture.setAssetToken(message.assetToken);
        picture.setSha256(Base64.getDecoder().decode(message.sha256));
        picture.setOtrKey(Base64.getDecoder().decode(message.otrKey));

        sendAsset(providerId, broadcastId, null, picture, botId);
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
            Logger.info("BroadcastV2Resource.get: broadcast: %s", broadcastId);

            Report ret = new Report();
            ret.broadcastId = broadcastId;
            ret.report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.exception("BroadcastV2Resource.get", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private void persist(UUID providerId, UUID broadcastId, UUID botId, UUID messageId) {
        broadcastDAO.insert(broadcastId, botId, providerId, messageId, BroadcastDAO.Type.SENT.ordinal());
    }

    private void sendAsset(UUID providerId, UUID broadcastId, @Nullable IGeneric preview, AssetBase audioAsset, UUID botId) {
        try {
            if (preview != null) {
                sender.send(preview, botId);
            }

            final UUID messageId = sender.send(audioAsset, botId);
            if (messageId != null) {
                persist(providerId, broadcastId, botId, messageId);
            }
        } catch (Exception e) {
            Logger.exception("Broadcast send", e);
        }
    }

    private void trace(BroadcastMessage message) throws JsonProcessingException {
        if (Logger.getLevel() == Level.FINE) {
            Logger.debug(objectMapper.writeValueAsString(message));
        }
    }
}
