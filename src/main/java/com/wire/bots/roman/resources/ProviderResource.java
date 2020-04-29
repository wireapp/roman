package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lambdaworks.crypto.SCryptUtil;
import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.ProviderClient;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.SignIn;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.dropwizard.validation.ValidationMethod;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Length;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Api
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ProviderResource {

    private final ProviderClient providerClient;
    private final ProvidersDAO providersDAO;

    public ProviderResource(DBI jdbi, ProviderClient providerClient) {
        this.providerClient = providerClient;
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
    }

    @POST
    @Path("/register")
    @ApiOperation(value = "Register as Wire Bot Developer")
    public Response register(@ApiParam @Valid _NewUser payload) {
        try {
            String name = payload.name;
            String email = payload.email;

            Response register = providerClient.register(name, email);

            Logger.debug("ProviderResource.register: login status %d", register.getStatus());

            if (register.getStatus() >= 400) {
                return Response.
                        ok(register.readEntity(String.class)).
                        status(register.getStatus()).
                        build();
            }

            Provider provider = register.readEntity(Provider.class);

            String hash = SCryptUtil.scrypt(payload.password, 16384, 8, 1);
            providersDAO.insert(name, provider.id, email, hash, provider.password);

            return Response.
                    ok(new ErrorMessage("Email was sent to: " + payload.email)).
                    status(register.getStatus()).
                    build();
        } catch (Exception e) {
            Logger.error("RegisterResource.register: %s", e);
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @POST
    @Path("/login")
    @ApiOperation(value = "Login as Wire Bot Developer")
    public Response login(@ApiParam @Valid SignIn payload) {
        try {
            Provider provider = providersDAO.get(payload.email);
            if (provider == null || !SCryptUtil.check(payload.password, provider.hash)) {
                return Response
                        .ok(new ErrorMessage("Wrong email or password"))
                        .status(401)
                        .build();
            }

            Response login = providerClient.login(provider.email, provider.password);

            Logger.debug("RegisterResource.login: login status %d", login.getStatus());

            if (login.getStatus() >= 400) {
                return Response.
                        ok(login.readEntity(String.class)).
                        status(login.getStatus()).
                        build();
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
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class _NewUser {
        @NotNull
        @Length(min = 3, max = 128)
        @JsonProperty
        public String name;

        @NotNull
        @Length(min = 8)
        @JsonProperty
        public String email;

        @NotNull
        @Length(min = 6, max = 24)
        @JsonProperty
        public String password;

        @JsonIgnore
        @ValidationMethod(message = "Malformed email")
        public boolean isEmail() {
            return email.contains("@") && email.contains(".");
        }
    }
}