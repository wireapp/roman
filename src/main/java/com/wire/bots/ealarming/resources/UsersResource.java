package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api
@Path("/users/{alertId}")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    private final AuthValidator validator;
    private final Alert2UserDAO alert2UserDAO;

    public UsersResource(Alert2UserDAO alert2UserDAO, AuthValidator validator) {
        this.alert2UserDAO = alert2UserDAO;
        this.validator = validator;
    }

    @GET
    @ApiOperation(value = "Get all Users for this Alert")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Users")})
    public Response get(@ApiParam @PathParam("alertId") int alertId) {
        try {
            List<Alert2User> select = alert2UserDAO.select(alertId);

            ArrayList<User> ret = new ArrayList<>();
            for (Alert2User alert2User : select) {
                User user = new User();
                user.userId = alert2User.userId;
                user.alertId = alert2User.alertId;
                user.messageStatus = alert2User.messageStatus;
                user.responseId = alert2User.responseId;
                user.escalated = alert2User.escalated;

                ret.add(user);
            }
            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.error("UsersResource.get(%d): %s", alertId, e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }

    @POST
    @ApiOperation(value = "Add Users for this Alert")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 200, message = "Users")})
    public Response post(@ApiParam @PathParam("alertId") int alertId,
                         @ApiParam @Valid ArrayList<UUID> users) {
        try {
            for (UUID userId : users) {
                alert2UserDAO.insert(alertId, userId, null, null, null);
            }
            return Response.
                    ok().
                    build();
        } catch (Exception e) {
            Logger.error("AlertResource.post: %s", e);
            return Response
                    .ok(e)
                    .status(500)
                    .build();
        }
    }
}