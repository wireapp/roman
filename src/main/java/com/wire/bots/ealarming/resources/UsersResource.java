package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.Result;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.AuthValidator;
import com.wire.bots.sdk.tools.Logger;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @ApiOperation(value = "Get all Users for this Alert", response = Result.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response get(@ApiParam @PathParam("alertId") int alertId,
                        @ApiParam @QueryParam("size") int size,
                        @ApiParam @QueryParam("page") int page) {
        try {
            Result<Alert2User> ret = new Result<>();
            ret.items = alert2UserDAO.listUsers(alertId);
            ret.page = page;
            ret.size = ret.items.size();

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