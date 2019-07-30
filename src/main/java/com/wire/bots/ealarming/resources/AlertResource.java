package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.AlertDAO;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.AlertResult;
import com.wire.bots.ealarming.model.Group;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api
@Path("/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource {
    private final AlertDAO alertDAO;
    private final Alert2UserDAO alert2UserDAO;
    private final AuthValidator validator;

    public AlertResource(AlertDAO alertDAO, Alert2UserDAO alert2UserDAO, AuthValidator validator) {
        this.alertDAO = alertDAO;
        this.alert2UserDAO = alert2UserDAO;
        this.validator = validator;
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
                    .ok(e)
                    .status(500)
                    .build();
        }
    }

    @PUT
    @Path("{alertId}")
    @ApiOperation(value = "Add Groups for this Alert")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Nothing")})
    public Response putGroups(@ApiParam @PathParam("alertId") int alertId,
                              @ApiParam @Valid ArrayList<Integer> groups) {
        try {
            for (Integer groupId : groups) {
                alertDAO.putGroup(alertId, groupId);
            }
            return Response.
                    ok().
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.putGroups: %s", e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }

    @GET
    @Path("{alertId}")
    @ApiOperation(value = "Get Alert  by its id")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Alert")})
    public Response get(@ApiParam @PathParam("alertId") int alertId) {
        try {
            Alert alert = alertDAO.get(alertId);
            if (alert == null) {
                return Response.
                        status(404).
                        build();
            }

            List<Group> groups = alertDAO.selectGroups(alertId);
            List<Alert2User> users = alert2UserDAO.selectUsers(alertId);

            AlertResult result = new AlertResult();
            result.alert = alert;
            result.groups = groups;
            result.users = users;

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.get(%d): %s", alertId, e);
            return Response
                    .ok(e)
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
            List<Alert> list = alertDAO.select();
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.getAll: %s", e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }
}