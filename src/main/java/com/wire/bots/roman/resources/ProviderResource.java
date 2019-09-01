package com.wire.bots.ealarming.resources;

import com.lambdaworks.crypto.SCryptUtil;
import com.wire.bots.ealarming.model.SignIn;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("/provider")
@Produces(MediaType.APPLICATION_JSON)
public class ProviderResource {

    private final WebTarget provider;

    public ProviderResource(DBI jdbi, Client jerseyClient) {
        provider = jerseyClient.target(Util.getHost())
                .path("provider");
    }

    @POST
    @Path("/register")
    @ApiOperation(value = "Register as Wire Bot Developer", response = Provider.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response register(@ApiParam @Valid Provider payload) {
        try {
            String hash = SCryptUtil.scrypt(payload.password, 16384, 8, 1);

            Provider p = new Provider();
            p.name = payload.name;
            p.email = payload.email;
            p.password = hash;
            p.description = "";
            p.url = "https://";

            Response register = provider.path("register")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(p, MediaType.APPLICATION_JSON));

            return Response.
                    ok(register.getEntity()).
                    status(register.getStatus()).
                    build();
        } catch (Exception e) {
            Logger.error("RegisterResource.register: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @POST
    @Path("/login")
    @ApiOperation(value = "Register as Wire Bot Developer", response = SignIn.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response login(@ApiParam @Valid SignIn payload) {
        try {
            Response login = provider.path("login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

            return Response.
                    ok(login.getEntity()).
                    status(login.getStatus()).
                    cookie(login.getCookies().get("zprovider")).
                    build();
        } catch (Exception e) {
            Logger.error("RegisterResource.login: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    static class _Provider {
        @NotNull
        String email;

        @NotNull
        String password;

        @NotNull
        String name;

        String url = "https://services.wire.com";

        String description = "Description";
    }
}