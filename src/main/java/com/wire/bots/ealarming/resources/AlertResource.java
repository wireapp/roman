package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.AlertDAO;
import com.wire.bots.ealarming.DAO.GroupsDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.ealarming.model.Alert;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.AlertResult;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.NotEmpty;
import org.skife.jdbi.v2.DBI;

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
    private final UserDAO userDAO;
    private final GroupsDAO groupsDAO;
    private final AuthValidator validator;

    public AlertResource(DBI jdbi, AuthValidator validator) {
        this.alertDAO = jdbi.onDemand(AlertDAO.class);
        this.alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
        this.userDAO = jdbi.onDemand(UserDAO.class);
        this.groupsDAO = jdbi.onDemand(GroupsDAO.class);
        this.validator = validator;
    }

    @POST
    @ApiOperation(value = "Create new Alert", response = Alert.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
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
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @PUT
    @Path("{alertId}")
    @ApiOperation(value = "Add Groups for this Alert", code = 201)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong", response = ErrorMessage.class)})
    public Response putGroups(@ApiParam @PathParam("alertId") int alertId,
                              @ApiParam @NotEmpty ArrayList<Integer> groups) {
        try {
            for (Integer groupId : groups) {
                alertDAO.putGroup(alertId, groupId);
                List<User> groupUsers = groupsDAO.selectUsers(groupId);
                for (User user : groupUsers) {
                    alert2UserDAO.insertUser(alertId, user.userId);
                }
            }
            return Response.
                    ok().
                    status(201).
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.putGroups: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @Path("{alertId}")
    @ApiOperation(value = "Get Alert by its id", response = AlertResult.class)
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
            result.users = new ArrayList<>();

            for (Alert2User alert2User : alert2UserDAO.selectUsers(alertId)) {
                User user = userDAO.get(alert2User.userId);
                if (user == null) {
                    user = new User();
                }

                user.userId = alert2User.userId;
                user.alertId = alert2User.alertId;
                user.messageStatus = alert2User.messageStatus;
                user.responseId = alert2User.responseId;
                user.escalated = alert2User.escalated;
                user.response = alert2User.response;

                result.users.add(user);
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