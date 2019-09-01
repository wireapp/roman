package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lambdaworks.crypto.SCryptUtil;
import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.SignIn;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import io.jsonwebtoken.Jwts;
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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Api
@Path("/provider")
@Produces(MediaType.APPLICATION_JSON)
public class ProviderResource {

    private final WebTarget provider;
    private final ProvidersDAO providersDAO;

    public ProviderResource(DBI jdbi, Client jerseyClient) {
        provider = jerseyClient
                .target(Util.getHost())
                .path("provider");
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
    }

    @POST
    @Path("/register")
    @ApiOperation(value = "Register as Wire Bot Developer")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response register(@ApiParam @Valid _NewUser payload) {
        try {
            _NewProvider newProvider = new _NewProvider();
            newProvider.name = payload.name;
            newProvider.email = payload.email;
            newProvider.description = "Description";
            newProvider.url = "https://wire.com";

            Response register = provider.path("register")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(newProvider, MediaType.APPLICATION_JSON));

            Logger.debug("ProviderResource.register: login status %d", register.getStatus());

            if (register.getStatus() >= 400) {
                return Response.
                        ok(register.readEntity(String.class)).
                        status(register.getStatus()).
                        build();
            }

            Provider provider = register.readEntity(Provider.class);

            String hash = SCryptUtil.scrypt(payload.password, 16384, 8, 1);
            UUID providerId = provider.id;
            String email = payload.email;
            String password = provider.password;
            providersDAO.insert(providerId, email, hash, password);

            return Response.
                    ok().
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
    @ApiOperation(value = "Login as Wire Bot Developer")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response login(@ApiParam @Valid SignIn payload) {
        try {
            Provider provider = providersDAO.get(payload.email);
            if (provider == null || !SCryptUtil.check(payload.password, provider.hash)) {
                return Response
                        .ok(new ErrorMessage("Wrong email or password"))
                        .status(403)
                        .build();
            }

            String jwt = Jwts.builder()
                    .setIssuer("https://wire.com")
                    .setSubject(provider.id.toString())
                    .signWith(Application.getKey())
                    .compact();

            return Response.
                    ok().
                    cookie(new NewCookie("zroman", jwt)).
                    build();

        } catch (Exception e) {
            Logger.error("RegisterResource.login: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _NewUser {
        @NotNull
        @JsonProperty
        public String name;

        @NotNull
        @JsonProperty
        public String email;

        @NotNull
        @JsonProperty
        public String password;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _NewProvider {
        @NotNull
        @JsonProperty
        public String name;

        @NotNull
        @JsonProperty
        public String email;

        @NotNull
        @JsonProperty
        public String url;

        @NotNull
        @JsonProperty
        public String description;

    }
}