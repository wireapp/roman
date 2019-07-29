package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.AlertDAO;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api
@Path("/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource {
    private final AlertDAO alertDAO;
    private final AuthValidator validator;

    public AlertResource(AlertDAO alertDAO, AuthValidator validator) {
        this.alertDAO = alertDAO;
        this.validator = validator;
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get Alert  by its id")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Alert")})
    public Response get(@ApiParam @PathParam("id") int id) {
        try {
            Alert alert = alertDAO.get(id);
            if (alert == null) {
                return Response.
                        status(404).
                        build();
            }

            return Response.
                    ok(alert).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.get(%d): %s", id, e);
            return Response
                    .ok(e.getMessage())
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get All Alerts ")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "List of Alerts")})
    public Response getAll() {
        try {
            List<Alert> list = alertDAO.list();
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.getAll: %s", e);
            return Response
                    .ok(e.getMessage())
                    .status(500)
                    .build();
        }
    }

    @POST
    @ApiOperation(value = "Create new Alert")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "New Alert")})
    public Response post(@ApiParam @Valid Alert alert) {
        try {
            int id = alertDAO.insert(alert.title,
                    alert.message,
                    alert.category,
                    alert.severity,
                    alert.creator,
                    alert.contact,
                    alert.starting,
                    alert.ending,
                    alert.status,
                    alert.responses);

            Alert ret = alertDAO.get(id);
            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.post: %s", e);
            return Response
                    .ok(e.getMessage())
                    .status(500)
                    .build();
        }
    }
}