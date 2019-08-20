package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.AlertDAO;
import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.model.*;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
            int alertId = alertDAO.insert(
                    payload.title,
                    payload.message,
                    payload.severity,
                    payload.attachment);

            for (String response : payload.responses) {
                int insert = alertDAO.addResponse(alertId, response);
                if (insert == 0)
                    Logger.warning("AlertResource.post: addResponse: alert: %s, response: %s. insert: %s",
                            alertId, response, insert);
            }

            List<Integer> groups = payload.groups;
            for (Integer groupId : groups) {
                alertDAO.putGroup(alertId, groupId);
                List<User> groupUsers = groupsDAO.selectUsers(groupId);
                for (User user : groupUsers) {
                    if (!payload.exclude.contains(user.userId)) {
                        int insert = alert2UserDAO.insertUser(alertId, user.userId);
                        if (insert == 0)
                            Logger.warning("AlertResource.post: insertUser: alert: %s, user: %s. insert: %s",
                                    alertId, user.userId, insert);
                    }
                }
            }

            List<UUID> userIds = payload.include;
            for (UUID userId : userIds) {
                int insert = alert2UserDAO.insertUser(alertId, userId);
                if (insert == 0)
                    Logger.warning("AlertResource.post: insertUser: alert: %s, user: %s. insert: %s",
                            alertId, userId, insert);
            }

            AlertResult result = new AlertResult();
            result.alert = alertDAO.get(alertId);
            result.groups = alertDAO.selectGroups(alertId);
            for (Group group : result.groups) {
                group.size = groupsDAO.size(group.id);
            }
            result.alert.responses = alertDAO.selectResponses(alertId);

            Logger.info("AlertResource.post: alert: %s, title: %s", alertId, payload.title);
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

            result.alert.responses = alertDAO.selectResponses(alertId);
            result.groups = alertDAO.selectGroups(alertId);
            for (Group group : result.groups) {
                group.size = groupsDAO.size(group.id);
            }

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
    @ApiOperation(value = "Get All Alerts", response = Result.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response getAll() {
        try {
            Result<Alert> ret = new Result<>();
            ret.items = alertDAO.list();
            ret.page = 1;
            ret.size = ret.items.size();

            for (Alert alert : ret.items) {
                alert.responses = alertDAO.selectResponses(alert.id);
            }

            return Response.
                    ok(ret).
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