package com.wire.bots.roman.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.model.OutgoingMessage;
import com.wire.xenon.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
            e.printStackTrace();
        }
        return Response
                .ok()
                .build();
    }
}