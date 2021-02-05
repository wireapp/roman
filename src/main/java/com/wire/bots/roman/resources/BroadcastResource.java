package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.BotsDAO;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.filters.ServiceTokenAuthorization;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Mention;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.MessageText;
import com.wire.bots.sdk.exceptions.MissingStateException;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.JwtException;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

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
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static com.wire.bots.roman.Const.APP_KEY;
import static com.wire.bots.roman.Const.PROVIDER_ID;

@Api
@Path("/broadcast")
@Produces(MediaType.APPLICATION_JSON)
public class BroadcastResource {
    private final DBI jdbi;
    private final ClientRepo repo;

    public BroadcastResource(DBI jdbi, ClientRepo repo) {
        this.jdbi = jdbi;
        this.repo = repo;
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
        try (WireClient client = repo.getClient(botId)) {
            switch (message.type) {
                case "text": {
                    MessageText text = new MessageText(message.text.data);
                    if (message.text.mentions != null) {
                        for (Mention mention : message.text.mentions)
                            text.addMention(mention.userId, mention.offset, mention.length);
                    }
                    client.send(text);
                }
                break;
                case "attachment": {
                    final byte[] decode = Base64.getDecoder().decode(message.attachment.data);
                    client.sendPicture(decode, message.attachment.mimeType);
                }
                break;
            }
            return true;
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