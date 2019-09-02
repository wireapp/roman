package com.wire.bots.roman.resources;

import com.wire.bots.roman.model.MessageOut;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.JwtException;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static com.wire.bots.roman.Tools.validateToken;

@Api
@Path("/conversation")
@Produces(MediaType.APPLICATION_JSON)
public class ConversationResource {

    private final ClientRepo repo;

    public ConversationResource(ClientRepo repo) {
        this.repo = repo;
    }

    @POST
    @ApiOperation(value = "Forward messages to Wire BE")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response send(@NotNull @HeaderParam("Authorization") String token,
                         @ApiParam @Valid MessageOut message) {
        try {
            String subject = validateToken(token);
            UUID botId = UUID.fromString(subject);

            Logger.info("ConversationResource.send: `%s` bot: %s", message.type, botId);

            try (WireClient client = repo.getClient(botId)) {
                switch (message.type) {
                    case "text": {
                        client.sendText(message.text);
                    }
                    break;
                }

                return Response.
                        ok().
                        build();
            }
        } catch (JwtException e) {
            Logger.warning("ConversationResource.send %s", e);
            return Response.
                    ok(new ErrorMessage("Invalid Authorization token")).
                    status(403).
                    build();
        } catch (Exception e) {
            Logger.error("ConversationResource.send: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}