package com.wire.bots.roman.resources;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.filters.ProxyAuthorization;
import com.wire.bots.roman.model.Attachment;
import com.wire.bots.roman.model.IncomingMessage;
import com.wire.bots.roman.model.Mention;
import com.wire.bots.roman.model.PostMessageResult;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.assets.*;
import com.wire.bots.sdk.exceptions.MissingStateException;
import com.wire.bots.sdk.models.AssetKey;
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
        UUID botId = (UUID) context.getProperty("botid");

        trace(message);

        try (WireClient client = repo.getClient(botId)) {
            return send(message, client);
        } catch (MissingStateException e) {
            Logger.info("ConversationResource bot: %s err: %s", botId, e);
            return Response.
                    ok(new ErrorMessage("Unknown bot. This bot might be deleted by the user")).
                    status(409).
                    build();
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

    private Response send(IncomingMessage message, WireClient client) throws Exception {
        PostMessageResult result = new PostMessageResult();

        switch (message.type) {
            case "text": {
                MessageText text = new MessageText(message.text.data);
                if (message.text.mentions != null) {
                    for (Mention mention : message.text.mentions)
                        text.addMention(mention.userId, mention.offset, mention.length);
                }
                client.send(text);
                result.messageId = text.getMessageId();
            }
            break;
            case "attachment": {
                if (message.attachment.mimeType.startsWith("image")) {
                    final byte[] decode = Base64.getDecoder().decode(message.attachment.data);
                    result.messageId = client.sendPicture(decode, message.attachment.mimeType);
                } else {
                    result.messageId = sendAttachment(message, client);
                }
            }
            break;
            case "poll": {
                if (message.poll.type.equals("create")) {
                    result.messageId = sendNewPoll(message, client);
                }
                if (message.poll.type.equals("confirmation")) {
                    result.messageId = sendPollConfirmation(message, client);
                }
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
    }

    private UUID sendPollConfirmation(IncomingMessage message, WireClient client) throws Exception {
        ButtonActionConfirmation confirmation = new ButtonActionConfirmation(
                message.poll.id,
                message.poll.offset.toString());

        Logger.info("poll.confirmation: pollId: %s, offset: %s", message.poll.id, message.poll.offset);

        client.send(confirmation, message.poll.userId);

        return confirmation.getMessageId();
    }

    private UUID sendNewPoll(IncomingMessage message, WireClient client) throws Exception {
        MessageText messageText = new MessageText(message.text.data);
        if (message.text.mentions != null) {
            for (Mention mention : message.text.mentions)
                messageText.addMention(mention.userId, mention.offset, mention.length);
        }

        Poll poll = new Poll(message.poll.id);
        poll.addText(messageText);

        int i = 0;
        for (String caption : message.poll.buttons) {
            poll.addButton("" + i++, caption);
        }

        Logger.info("poll.create: id: %s", message.poll.id);

        client.send(poll);

        return poll.getMessageId();
    }

    private UUID sendAttachment(IncomingMessage message, WireClient client) throws Exception {
        UUID messageId = UUID.randomUUID();

        final Attachment attachment = message.attachment;
        final byte[] decode = Base64.getDecoder().decode(attachment.data);

        FileAssetPreview preview = new FileAssetPreview(attachment.filename, attachment.mimeType, decode.length, messageId);
        FileAsset asset = new FileAsset(decode, attachment.mimeType, messageId);

        client.send(preview);
        final AssetKey assetKey = client.uploadAsset(asset);
        asset.setAssetKey(assetKey.key);
        asset.setAssetToken(assetKey.token);
        client.send(asset);
        return messageId;
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