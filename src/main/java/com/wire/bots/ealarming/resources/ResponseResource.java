package com.wire.bots.ealarming.resources;

import com.wire.bots.ealarming.DAO.Alert2UserDAO;
import com.wire.bots.ealarming.Service;
import com.wire.bots.ealarming.model.Alert2User;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Api
@Path("/response/{token}")
@Produces(MediaType.TEXT_HTML)
public class ResponseResource {
    private final Alert2UserDAO alert2UserDAO;

    public ResponseResource(DBI jdbi) {
        this.alert2UserDAO = jdbi.onDemand(Alert2UserDAO.class);
    }

    @GET
    @ApiOperation(value = "Response landing page")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid token")})
    public Response get(@PathParam("token") String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(Service.getKey())
                    .parseClaimsJws(token);

            Claims body = claimsJws.getBody();

            Integer alertId = (Integer) body.get("alertId");
            UUID userId = UUID.fromString((String) body.get("userId"));
            UUID messageId = UUID.fromString((String) body.get("messageId"));
            String response = (String) body.get("response");

            Logger.info("ResponseResource: alert: %s, user: %s, message: %s, response: %s",
                    alertId,
                    userId,
                    messageId,
                    response);

            int update = alert2UserDAO.insertStatus(alertId, userId, Alert2User.Type.RESPONDED.ordinal(), messageId, response);
            if (update == 0)
                return Response.
                        ok("You've already voted, ffs").
                        build();

            return Response.
                    ok(response).
                    build();
        } catch (SignatureException e) {
            Logger.warning("ResponseResource.get: %s", e);
            return Response.
                    ok(new ErrorMessage("Invalid token")).
                    status(400).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ResponseResource.get: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}
