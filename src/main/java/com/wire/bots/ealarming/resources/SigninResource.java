package com.wire.bots.ealarming.resources;

import com.lambdaworks.crypto.SCryptUtil;
import com.wire.bots.ealarming.DAO.AdminsDAO;
import com.wire.bots.ealarming.Service;
import com.wire.bots.ealarming.model.SignIn;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Api
@Path("/signin")
@Produces(MediaType.APPLICATION_JSON)
public class SigninResource {
    private final AdminsDAO adminsDAO;

    public SigninResource(DBI jdbi) {
        adminsDAO = jdbi.onDemand(AdminsDAO.class);
    }

    @POST
    @ApiOperation(value = "Signin")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "Wrong email or password", response = ErrorMessage.class)})
    public Response signin(@ApiParam @Valid SignIn signIn) {
        try {
            String hashed = adminsDAO.getHash(signIn.email);
            if (hashed == null || !SCryptUtil.check(signIn.password, hashed)) {
                return Response
                        .ok(new ErrorMessage("Wrong email or password"))
                        .status(403)
                        .build();
            }

            String jwt = Jwts.builder()
                    .setIssuer("https://wire.com/")
                    .setSubject("" + adminsDAO.getUserId(signIn.email))
                    .signWith(Service.getKey())
                    .compact();

            return Response.
                    ok().
                    cookie(new NewCookie("Authorization", jwt)).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("SigninResource.signin : %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}
