package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.Service;
import com.wire.bots.roman.model.SignIn;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import com.wire.bots.sdk.tools.Util;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.UUID;

import static com.wire.bots.roman.Tools.validateToken;

@Api
@Path("/provider/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private final WebTarget servicesTarget;
    private final WebTarget providerTarget;
    private final ProvidersDAO providersDAO;

    public ServiceResource(DBI jdbi, Client jerseyClient) {
        providersDAO = jdbi.onDemand(ProvidersDAO.class);

        providerTarget = jerseyClient.target(Util.getHost())
                .path("provider");
        servicesTarget = providerTarget
                .path("services");
    }

    @POST
    @ApiOperation(value = "Register new Service", response = _Result.class)
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Not authenticated")})
    public Response create(@ApiParam(hidden = true) @NotNull @CookieParam("zroman") String cookie,
                           @ApiParam @Valid _NewService payload) {
        try {
            String subject = validateToken(cookie);

            Logger.debug("ServiceResource.create: provider: %s", subject);

            UUID providerId = UUID.fromString(subject);

            Provider provider = providersDAO.get(providerId);  //todo: pull from the token

            Logger.debug("ServiceResource.create: provider: %s, %s", provider.id, provider.email);

            SignIn signIn = new SignIn();
            signIn.email = provider.email;
            signIn.password = provider.password;

            Response login = providerTarget.path("login")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(signIn, MediaType.APPLICATION_JSON));

            Logger.debug("ServiceResource.create: login status %d", login.getStatus());

            if (login.getStatus() >= 400) {
                return Response.
                        ok(login.readEntity(String.class)).
                        status(login.getStatus()).
                        build();
            }

            Service service = new Service();
            service.name = payload.name;
            service.pubkey = String.format("%s\n%s\n%s", "-----BEGIN PUBLIC KEY-----", getPubkey(), "-----END PUBLIC KEY-----");
            service.baseUrl = String.format("https://services.%s/roman", Util.getDomain());
            service.description = "Description";
            service.summary = "Summary";
            service.tags = new String[]{"tutorial"};

            Logger.debug("ServiceResource.create: pub key: `%s`", service.pubkey);

            Response create = servicesTarget
                    .request(MediaType.APPLICATION_JSON)
                    .cookie(login.getCookies().get("zprovider"))
                    .post(Entity.entity(service, MediaType.APPLICATION_JSON));

            Logger.debug("ServiceResource.create: create service %s, status: %d", service.name, create.getStatus());

            if (create.getStatus() >= 400) {
                return Response.
                        ok(create.readEntity(String.class)).
                        status(create.getStatus()).
                        build();
            }

            service = create.readEntity(Service.class);

            Logger.debug("ServiceResource.create: create service %s, status: %d", service.id, create.getStatus());

            _UpdateService updateService = new _UpdateService();
            updateService.enabled = true;
            updateService.password = provider.password;

            Response update = servicesTarget
                    .path(service.id.toString())
                    .path("connection")
                    .request(MediaType.APPLICATION_JSON)
                    .cookie(login.getCookies().get("zprovider"))
                    .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));

            Logger.debug("ServiceResource.create: update service %s, status: %d", service.id, update.getStatus());

            if (update.getStatus() >= 400) {
                return Response.
                        ok(update.readEntity(String.class)).
                        status(update.getStatus()).
                        build();
            }

            providersDAO.add(providerId, payload.url, service.token);   //todo sanity check on payload.url

            _Result result = new _Result();
            result.token = service.token;
            result.code = String.format("%s:%s", providerId, service.id);

            Logger.info("ServiceResource.create: service token %s, code: %s", result.token, result.code);

            return Response.
                    ok(result).
                    status(update.getStatus()).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ServiceResource.create: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    private String getPubkey() throws IOException {
        PublicKey publicKey = getPublicKey(String.format("services.%s", Util.getDomain()));
        if (publicKey != null)
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "";
    }

    private static PublicKey getPublicKey(String hostname) throws IOException {
        SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(hostname, 443);
        socket.startHandshake();
        Certificate[] certs = socket.getSession().getPeerCertificates();
        Certificate cert = certs[0];
        return cert.getPublicKey();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _UpdateService {
        @NotNull
        public String password;

        @NotNull
        public boolean enabled;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _NewService {
        @NotNull
        @JsonProperty
        public String name;

        @NotNull
        @JsonProperty
        public String url;
    }

    static class _Result {
        @NotNull
        @JsonProperty
        public String code;

        @NotNull
        @JsonProperty("inbound_token")
        public String token;
    }
}