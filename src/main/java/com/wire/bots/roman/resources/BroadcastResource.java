package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.BroadcastDAO;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.filters.ServiceTokenAuthorization;
import com.wire.bots.roman.model.AssetMeta;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Report;
import com.wire.lithium.server.monitoring.MDCUtils;
import com.wire.xenon.assets.*;
import com.wire.xenon.backend.models.ErrorMessage;
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
import java.util.Base64;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            final UUID broadcastId = UUID.randomUUID();

            MDCUtils.put("broadcastId", broadcastId);
            Logger.info("BroadcastResource.post: `%s`", message.type);

            switch (message.type) {
                case "text": {
                    broadcastText(message, providerId, broadcastId);
                    break;
                }
                case "attachment": {
                    if (message.attachment.mimeType.startsWith("audio")) {
                        broadcastAudio(message, providerId, broadcastId);
                    } else if (message.attachment.mimeType.startsWith("image")) {
                        broadcastPicture(message, providerId, broadcastId);
                    } else {
                        broadcastFile(message, providerId, broadcastId);
                    }
                    break;
                }
                case "call": {
                    broadcastCall(message, providerId, broadcastId);
                    break;
                }
            }

            Report ret = new Report();
            ret.broadcastId = broadcastId;
            ret.report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.exception("BroadcastResource.post", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private void broadcastCall(IncomingMessage message, UUID providerId, UUID broadcastId) {
        for (UUID botId : botsDAO.getBotIds(providerId)) {
            broadcast.submit(() -> {
                try {
                    final UUID messageId = sender.sendCall(message, botId);
                    if (messageId != null) {
                        persist(providerId, broadcastId, botId, messageId);
                    }
                } catch (Exception e) {
                    Logger.exception("Broadcast", e);
                }
            });
        }
    }

    private void broadcastPicture(IncomingMessage message, UUID providerId, UUID broadcastId) {
        final Picture picture = new Picture(UUID.randomUUID(), message.attachment.mimeType);

        final List<UUID> botIds = botsDAO.getBotIds(providerId);

        setAssetMetadata(picture, message.attachment.meta);

        for (UUID botId : botIds) {
            broadcast.submit(() -> send(providerId, broadcastId, picture, botId));
        }
    }

    private void setAssetMetadata(AssetBase asset, AssetMeta meta) {
        asset.setAssetKey(meta.assetId);
        asset.setAssetToken(meta.assetToken);
        asset.setSha256(Base64.getDecoder().decode(meta.sha256));
        asset.setOtrKey(Base64.getDecoder().decode(meta.otrKey));
    }

    private void broadcastAudio(IncomingMessage message, UUID providerId, UUID broadcastId) {
        final AudioPreview preview = new AudioPreview(
                message.attachment.name,
                message.attachment.mimeType,
                message.attachment.duration,
                message.attachment.levels,
                message.attachment.size.intValue());

        final AudioAsset audioAsset = new AudioAsset(preview.getMessageId(), message.attachment.mimeType);

        final List<UUID> botIds = botsDAO.getBotIds(providerId);

        setAssetMetadata(audioAsset, message.attachment.meta);

        for (UUID botId : botIds) {
            broadcast.submit(() -> send(providerId, broadcastId, preview, audioAsset, botId));
        }
    }

    private void broadcastFile(IncomingMessage message, UUID providerId, UUID broadcastId) {
        final UUID messageId = UUID.randomUUID();
        FileAssetPreview preview = new FileAssetPreview(message.attachment.name,
                message.attachment.mimeType,
                message.attachment.size.intValue(),
                messageId);

        FileAsset fileAsset = new FileAsset(messageId, message.attachment.mimeType);

        final List<UUID> botIds = botsDAO.getBotIds(providerId);

        setAssetMetadata(fileAsset, message.attachment.meta);

        for (UUID botId : botIds) {
            broadcast.submit(() -> send(providerId, broadcastId, preview, fileAsset, botId));
        }
    }

    private void broadcastText(IncomingMessage message, UUID providerId, UUID broadcastId) {
        for (UUID botId : botsDAO.getBotIds(providerId)) {
            broadcast.submit(() -> {
                try {
                    final UUID messageId = sender.sendText(message, botId);
                    if (messageId != null) {
                        persist(providerId, broadcastId, botId, messageId);
                    }
                } catch (Exception e) {
                    Logger.exception("Broadcast", e);
                }
            });
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
            Logger.info("BroadcastResource.get: broadcast: %s", broadcastId);

            Report ret = new Report();
            ret.broadcastId = broadcastId;
            ret.report = broadcastDAO.report(broadcastId);

            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.exception("BroadcastResource.get", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private void send(UUID providerId, UUID broadcastId, AudioPreview preview, AudioAsset audioAsset, UUID botId) {
        try {
            sender.send(preview, botId);
            final UUID messageId = sender.send(audioAsset, botId);
            if (messageId != null) {
                persist(providerId, broadcastId, botId, messageId);
            }
        } catch (Exception e) {
            Logger.exception("Broadcast send", e);
        }
    }

    private void send(UUID providerId, UUID broadcastId, FileAssetPreview preview, FileAsset fileAsset, UUID botId) {
        try {
            sender.send(preview, botId);
            final UUID messageId = sender.send(fileAsset, botId);
            if (messageId != null) {
                persist(providerId, broadcastId, botId, messageId);
            }
        } catch (Exception e) {
            Logger.exception("Broadcast send", e);
        }
    }

    private void send(UUID providerId, UUID broadcastId, Picture picture, UUID botId) {
        try {
            final UUID messageId = sender.send(picture, botId);
            if (messageId != null) {
                persist(providerId, broadcastId, botId, messageId);
            }
        } catch (Exception e) {
            Logger.exception("Broadcast send", e);
        }
    }

    private void persist(UUID providerId, UUID broadcastId, UUID botId, UUID messageId) {
        broadcastDAO.insert(broadcastId, botId, providerId, messageId, BroadcastDAO.Type.SENT.ordinal());
    }

    private void trace(IncomingMessage message) throws JsonProcessingException {
        if (Logger.getLevel() == Level.FINE) {
            Logger.debug(objectMapper.writeValueAsString(message));
        }
    }
}
