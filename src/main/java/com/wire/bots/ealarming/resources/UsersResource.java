package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.Result;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static com.wire.bots.ealarming.Tools.validateToken;

@Api
@Path("/users/{alertId}")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    private final Alert2UserDAO alert2UserDAO;

    public UsersResource(DBI jdbi) {
        this.alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
    }

    @GET
    @ApiOperation(value = "Get Delivery Statuses for this Alert", response = Result.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response get(@ApiParam(hidden = true) @CookieParam("Authorization") String token,
                        @PathParam("alertId") int alertId,
                        @ApiParam(defaultValue = "10") @QueryParam("size") int size,
                        @ApiParam(defaultValue = "1") @QueryParam("page") int page,
                        @QueryParam("user") UUID userId) {

        try {
            String subject = validateToken(token);

            Result<Alert2User> ret = new Result<>();
            ret.items = userId != null
                    ? alert2UserDAO.listStatuses(alertId, userId)
                    : alert2UserDAO.listUsers(alertId);
            ret.page = page;
            ret.size = ret.items.size();

            Logger.info("UsersResource.get(%d, %s) Admin: %s", alertId, userId, subject);

            return Response.
                    ok(ret).
                    build();
        } catch (SignatureException e) {
            Logger.warning("UsersResource.get(%d, %s) %s", alertId, userId, e);
            return Response.
                    ok(new ErrorMessage("Not authenticated")).
                    status(403).
                    build();
        }
    }
}