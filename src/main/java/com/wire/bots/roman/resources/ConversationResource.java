package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.filters.ProxyAuthorization;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.PostMessageResult;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.ButtonActionConfirmation;
import com.wire.bots.sdk.assets.MessageText;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.assets.Poll;
import com.wire.bots.sdk.exceptions.MissingStateException;
import com.wire.bots.sdk.server.model.Conversation;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;

@Api
@Path("/conversation")
@Produces(MediaType.APPLICATION_JSON)
public class ConversationResource {
    private final ClientRepo repo;

    public ConversationResource(ClientRepo repo) {
        this.repo = repo;
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
        try {
            trace(message);

            UUID botId = (UUID) context.getProperty("botid");

            return send(message, botId);
        } catch (Exception e) {
            Logger.error("ConversationResource.post: %s", e);
            e.printStackTrace();
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
        try (WireClient client = repo.getClient((UUID) context.getProperty("botid"))) {
            return Response
                    .ok(client.getConversation())
                    .build();
        } catch (Exception e) {
            Logger.error("ConversationResource.get: %s", e);
            e.printStackTrace();
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private Response send(IncomingMessage message, UUID botId) throws Exception {
        PostMessageResult result = new PostMessageResult();

        try (WireClient client = repo.getClient(botId)) {
            switch (message.type) {
                case "text": {
                    result.messageId = client.sendText(message.text);
                }
                break;
                case "image": {
                    Picture picture = new Picture(Base64.getDecoder().decode(message.image));
                    result.messageId = client.sendPicture(picture.getImageData(), picture.getMimeType());
                }
                break;
                case "poll.new": {
                    Poll poll = new Poll();
                    poll.setMessageId(message.poll.id);
                    poll.addText(message.poll.body);

                    StringBuilder sb = new StringBuilder(message.poll.body);
                    for (int i = 0; i < message.poll.buttons.size(); i++) {
                        final String caption = message.poll.buttons.get(i);

                        sb.append(String.format("\n[%s](%s)", caption, i));
                        poll.addButton("" + i, caption);
                    }

                    MessageText text = new MessageText(sb.toString())
                            .setMessageId(message.poll.id);

                    try {
                        client.send(text);
                        client.send(poll);
                    } catch (Exception ignore) {

                    }

                    result.messageId = text.getMessageId();
                }
                break;
                case "poll.action.confirmation": {
                    ButtonActionConfirmation confirmation = new ButtonActionConfirmation(message.poll.id, message.poll.offset);
                    try {
                        client.sendDirectPicture(confirmation, message.poll.userId);
                    } catch (Exception e) {

                    }
                    result.messageId = confirmation.getMessageId();
                }
                break;
                default:
                    return Response.
                            ok(new ErrorMessage("Unknown message type: " + message.type)).
                            status(400).
                            build();
            }

            return Response.
                    ok(result).
                    build();
        } catch (MissingStateException e) {
            Logger.info("ConversationResource bot: %s err: %s", botId, e);
            return Response.
                    ok(new ErrorMessage("Unknown bot. This bot might be deleted by the user")).
                    status(409).
                    build();
        } catch (Exception e) {
            Logger.error("ConversationResource bot: %s err: %s", botId, e);
            return Response.
                    ok(new ErrorMessage(e.getMessage())).
                    status(503).
                    build();
        }
    }

    private void trace(IncomingMessage message) {
        try {
            if (Logger.getLevel() == Level.FINE) {
                ObjectMapper mapper = new ObjectMapper();
                Logger.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
            }
        } catch (Exception ignore) {

        }
    }
}