package com.wire.bots.roman.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.xenon.tools.Logger;
import io.swagger.annotations.*;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Api
@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MessagesResource {
    @POST
    @ApiOperation(value = "Dummy. Bot developer should implement this", authorizations = {@Authorization("Bearer")})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "All good")})
    public Response post(@ApiParam @NotNull @Valid OutgoingMessage message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Logger.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
        } catch (JsonProcessingException e) {
            Logger.exception("MessagesResource exception during parsing.", e);
        }
        return Response
                .ok()
                .build();
    }
}
