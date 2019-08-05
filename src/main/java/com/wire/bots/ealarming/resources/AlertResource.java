package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.AlertDAO;
import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.ealarming.model.AlertPayload;
import com.wire.bots.ealarming.model.AlertResult;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api
@Path("/alerts")
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource {
    private final AlertDAO alertDAO;
    private final Alert2UserDAO alert2UserDAO;
    private final GroupsDAO groupsDAO;
    private final AuthValidator validator;

    public AlertResource(DBI jdbi, AuthValidator validator) {
        this.alertDAO = jdbi.onDemand(AlertDAO.class);
        this.alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
        this.groupsDAO = jdbi.onDemand(GroupsDAO.class);
        this.validator = validator;
    }

    @POST
    @ApiOperation(value = "Create new Alert", response = AlertResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response post(@ApiParam @Valid AlertPayload payload) {
        try {
            Alert alert = payload.alert;
            int alertId = alertDAO.insert(alert.title,
                    alert.message,
                    alert.category,
                    alert.severity,
                    alert.creator,
                    alert.contact,
                    alert.starting,
                    alert.ending,
                    alert.status,
                    alert.responses);

            ArrayList<Integer> groups = payload.groups;
            for (Integer groupId : groups) {
                alertDAO.putGroup(alertId, groupId);
                List<User> groupUsers = groupsDAO.selectUsers(groupId);
                for (User user : groupUsers) {
                    if (!payload.exclude.contains(user.userId)) {
                        alert2UserDAO.insertUser(alertId, user.userId);
                    }
                }
            }

            ArrayList<UUID> userIds = payload.include;
            for (UUID userId : userIds) {
                alert2UserDAO.insertUser(alertId, userId);
            }

            AlertResult result = new AlertResult();
            result.alert = alertDAO.get(alertId);
            result.groups = alertDAO.selectGroups(alertId);

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.post: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @Path("{alertId}")
    @ApiOperation(value = "Get Alert by id", response = AlertResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response get(@ApiParam @PathParam("alertId") int alertId) {
        try {
            AlertResult result = new AlertResult();

            result.alert = alertDAO.get(alertId);
            if (result.alert == null) {
                return Response.
                        status(404).
                        build();
            }

            result.groups = alertDAO.selectGroups(alertId);

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.get(%d): %s", alertId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get All Alerts", response = Alert.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response getAll() {
        try {
            List<Alert> list = alertDAO.list();
            return Response.
                    ok(list).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.getAll: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}