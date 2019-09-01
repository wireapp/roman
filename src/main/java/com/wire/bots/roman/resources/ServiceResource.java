package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.model.Service;
import com.wire.bots.ealarming.model.UpdateService;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Api
@Path("/provider/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private final WebTarget services;

    public ServiceResource(DBI jdbi, Client jerseyClient) {
        services = jerseyClient.target(Util.getHost())
                .path("provider")
                .path("services");
    }

    @POST
    @ApiOperation(value = "Register new Service", response = Service.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response create(@ApiParam(hidden = true) @NotNull @CookieParam("zprovider") String cookie,
                           @ApiParam @Valid Service payload) {
        try {
            payload.pubkey = new String(Util.getResource("pubkey.pem"));
            
            Response response = services
                    .request(MediaType.APPLICATION_JSON)
                    .cookie("zprovider", cookie)
                    .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

            return Response.
                    ok(response.getEntity()).
                    status(response.getStatus()).
                    build();
        } catch (Exception e) {
            Logger.error("ServiceResource.create: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @PUT
    @Path("/{serviceId}/connection")
    @ApiOperation(value = "Enable you Service", response = Service.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response update(@ApiParam(hidden = true) @NotNull @CookieParam("zprovider") String cookie,
                           @PathParam("serviceId") UUID serviceID,
                           @ApiParam @Valid UpdateService payload) {
        try {
            Response login = services
                    .path(serviceID.toString())
                    .path("connection")
                    .request(MediaType.APPLICATION_JSON)
                    .cookie("zprovider", cookie)
                    .put(Entity.entity(payload, MediaType.APPLICATION_JSON));

            return Response.
                    ok(login.getEntity()).
                    status(login.getStatus()).
                    build();
        } catch (Exception e) {
            Logger.error("ServiceResource.update: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}