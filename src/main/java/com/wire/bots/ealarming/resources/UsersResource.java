package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.Service;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.ealarming.model.Result;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("/users/{alertId}")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    private final Alert2UserDAO alert2UserDAO;

    public UsersResource(DBI jdbi) {
        this.alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
    }

    @GET
    @ApiOperation(value = "Get all Users for this Alert", response = Result.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Something went wrong")})
    public Response get(@CookieParam("Authorization") String auth,
                        @ApiParam @PathParam("alertId") int alertId,
                        @ApiParam @QueryParam("size") int size,
                        @ApiParam @QueryParam("page") int page) {

        try {
            String subject = Jwts.parser()
                    .setSigningKey(Service.getKey())
                    .parseClaimsJws(auth)
                    .getBody()
                    .getSubject();

            Result<Alert2User> ret = new Result<>();
            ret.items = alert2UserDAO.listUsers(alertId);
            ret.page = page;
            ret.size = ret.items.size();

            Logger.info("UsersResource.get(%d) Admin: %s", alertId, subject);

            return Response.
                    ok(ret).
                    build();
        } catch (SignatureException e) {
            Logger.warning("UsersResource.get(%d) %s", alertId, e);
            return Response.
                    ok(new ErrorMessage("Not authenticated")).
                    status(403).
                    build();
        }
    }
}