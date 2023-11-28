package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.Sender;
import com.wire.bots.roman.filters.ProxyAuthorization;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.PostMessageResult;
import com.wire.xenon.backend.models.Conversation;
import com.wire.xenon.backend.models.ErrorMessage;
import com.wire.xenon.exceptions.MissingStateException;
import com.wire.xenon.tools.Logger;
import io.swagger.annotations.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;
import java.util.logging.Level;

import static com.wire.bots.roman.Const.BOT_ID;

@Api
@Path("/conversation")
@Produces(MediaType.APPLICATION_JSON)
public class ConversationResource {
    private final Sender sender;

    public ConversationResource(Sender sender) {
        this.sender = sender;
    }

    @POST
    @ApiOperation(value = "Post message on Wire", authorizations = {@Authorization("Bearer")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = PostMessageResult.class, message = "MessageId"),
            @ApiResponse(code = 403, response = ErrorMessage.class, message = "Not authenticated"),
            @ApiResponse(code = 409, response = ErrorMessage.class, message = "Unknown bot. This bot might be deleted by the user")
    })
    @ProxyAuthorization
    @Metered
    public Response post(@Context ContainerRequestContext context,
                         @ApiParam @NotNull @Valid IncomingMessage message) {
        final UUID botId = (UUID) context.getProperty(BOT_ID);

        trace(message);

        try {
            PostMessageResult result = new PostMessageResult();
            result.messageId = sender.send(message, botId);

            return Response
                    .ok(result)
                    .build();
        } catch (MissingStateException e) {
            Logger.warning("ConversationResource err: %s", e.getMessage());
            return Response.
                    ok(new ErrorMessage("Unknown bot. This bot might be deleted by the user")).
                    status(409).
                    build();
        } catch (Exception e) {
            Logger.exception("ConversationResource.post: %s", e, e.getMessage());
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get conversation data", authorizations = {@Authorization("Bearer")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Conversation.class, message = "Conversation"),
            @ApiResponse(code = 403, message = "Not authenticated"),
            @ApiResponse(code = 409, message = "Unknown bot. This bot might be deleted by the user")
    })
    @ProxyAuthorization
    @Metered
    public Response get(@Context ContainerRequestContext context) {
        final UUID botId = (UUID) context.getProperty(BOT_ID);

        try {
            Conversation conversation = sender.getConversation(botId);
            return Response
                    .ok(conversation)
                    .build();
        } catch (Exception e) {
            Logger.exception("ConversationResource.get: %s", e, e.getMessage());
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private void trace(IncomingMessage message) {
        try {
            if (Logger.getLevel() == Level.FINE) {
                ObjectMapper mapper = new ObjectMapper();
                Logger.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
            }
        } catch (Exception ignore) {

        }
    }
}
