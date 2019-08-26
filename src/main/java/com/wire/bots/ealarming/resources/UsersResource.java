package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.DAO.UserDAO;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.Result;
import com.wire.bots.ealarming.model.User;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Api
@Path("/users/{alertId}")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    private final UserDAO userDAO;
    private final AuthValidator validator;
    private final Alert2UserDAO alert2UserDAO;

    public UsersResource(Alert2UserDAO alert2UserDAO, UserDAO userDAO, AuthValidator validator) {
        this.alert2UserDAO = alert2UserDAO;
        this.userDAO = userDAO;
        this.validator = validator;
    }

    @GET
    @ApiOperation(value = "Get all Users for this Alert", response = Result.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response get(@ApiParam @PathParam("alertId") int alertId) {
        try {
            List<Alert2User> select = alert2UserDAO.selectUsers(alertId);

            Result<User> ret = new Result<>();
            ret.items = new ArrayList<>();
            ret.page = 1;
            ret.size = select.size();

            for (Alert2User alert2User : select) {
                User user = userDAO.get(alert2User.userId);
                if (user == null) {
                    user = new User();
                }

                user.userId = alert2User.userId;
                user.alertId = alert2User.alertId;
                user.messageStatus = alert2User.messageStatus;
                user.escalated = alert2User.escalated;
                user.response = alert2User.response;

                ret.items.add(user);
            }
            return Response.
                    ok(ret).
                    build();
        } catch (Exception e) {
            Logger.error("UsersResource.get(%d): %s", alertId, e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}