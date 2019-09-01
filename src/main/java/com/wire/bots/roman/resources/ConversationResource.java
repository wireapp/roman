package com.wire.bots.roman.resources;

import com.wire.bots.roman.DAO.AdminsDAO;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.Service;
import com.wire.bots.roman.model.SignIn;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static com.wire.bots.roman.Tools.validateToken;

@Api
@Path("/provider/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private final WebTarget servicesTarget;
    private final WebTarget providerTarget;
    private final AdminsDAO adminsDAO;

    public ServiceResource(DBI jdbi, Client jerseyClient) {
        adminsDAO = jdbi.onDemand(AdminsDAO.class);

        providerTarget = jerseyClient.target(Util.getHost())
                .path("provider");
        servicesTarget = providerTarget
                .path("services");
    }

    @POST
    @ApiOperation(value = "Register new Service", response = Service.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response create(@ApiParam(hidden = true) @NotNull @CookieParam("zroman") String cookie,
                           @ApiParam @Valid _Payload payload) {
        try {
            String subject = validateToken(cookie);

            UUID providerId = UUID.fromString(subject);

            Provider provider = adminsDAO.get(providerId);  //todo: pull from the token

            SignIn signIn = new SignIn();
            signIn.email = provider.email;
            signIn.password = provider.password;

            Response login = providerTarget.path("login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(signIn, MediaType.APPLICATION_JSON));

            Service service = new Service();

            service.name = payload.name;
            service.pubkey = new String(Util.getResource("pubkey.pem"));
            service.baseUrl = String.format("https://services.%s/roman", Util.getDomain());

            Response create = servicesTarget
                    .request(MediaType.APPLICATION_JSON)
                    .cookie(login.getCookies().get("zprovider"))
                    .post(Entity.entity(service, MediaType.APPLICATION_JSON));

            service = create.readEntity(Service.class);

            _UpdateService updateService = new _UpdateService();
            updateService.enabled = true;
            updateService.password = provider.password;

            Response update = servicesTarget
                    .path(service.id.toString())
                    .path("connection")
                    .request(MediaType.APPLICATION_JSON)
                    .cookie(login.getCookies().get("zprovider"))
                    .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));

            adminsDAO.add(providerId, payload.url, service.token);

            _Result result = new _Result();
            result.authorization = service.token;
            result.code = String.format("%s:%s", providerId, service.id);

            return Response.
                    ok(result).
                    status(update.getStatus()).
                    build();
        } catch (Exception e) {
            Logger.error("ServiceResource.create: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    static class _UpdateService {
        @NotNull
        String password;

        @NotNull
        boolean enabled;
    }

    static class _Payload {
        @NotNull
        String name;

        @NotNull
        String url;
    }

    static class _Result {
        @NotNull
        String code;

        @NotNull
        String authorization;
    }

}