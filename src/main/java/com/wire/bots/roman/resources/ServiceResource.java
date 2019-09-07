package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.ImageProcessor;
import com.wire.bots.roman.ProviderClient;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.Service;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.dropwizard.validation.ValidationMethod;
import io.jsonwebtoken.JwtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Length;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;

import static com.wire.bots.roman.Tools.validateToken;

@Api
@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private final ProviderClient providerClient;
    private final DBI jdbi;

    public ServiceResource(DBI jdbi, ProviderClient providerClient) {
        this.providerClient = providerClient;
        this.jdbi = jdbi;
    }

    @POST
    @ApiOperation(value = "Create new Service", response = _Result.class)
    public Response create(@ApiParam(hidden = true) @CookieParam("zroman") String token,
                           @ApiParam @Valid _NewService payload) {
        try {
            if (token == null) {
                Response.
                        ok(new ErrorMessage("Not Authenticated")).
                        status(403).
                        build();
            }

            String subject = validateToken(token);

            Logger.debug("ServiceResource.create: provider: %s", subject);

            UUID providerId = UUID.fromString(subject);
            Provider provider = jdbi.onDemand(ProvidersDAO.class).get(providerId);

            Logger.debug("ServiceResource.create: provider: %s, %s", provider.id, provider.email);

            Response login = providerClient.login(provider.email, provider.password);

            Logger.debug("ServiceResource.create: login status %d", login.getStatus());

            if (login.getStatus() >= 400) {
                return Response.
                        ok(login.readEntity(String.class)).
                        status(login.getStatus()).
                        build();
            }

            NewCookie cookie = login.getCookies().get("zprovider");

            Service service = new Service();
            service.name = payload.name;

            if (payload.avatar != null) {
                byte[] image = Base64.getDecoder().decode(payload.avatar);
                Picture mediumImage = ImageProcessor.getMediumImage(new Picture(image));
                String key = providerClient.uploadProfilePicture(cookie, mediumImage.getImageData(), mediumImage.getMimeType());
                service.assets.get(0).key = key;
                service.assets.get(1).key = key;
            }

            Response create = providerClient.createService(cookie, service);

            if (create.getStatus() >= 400) {
                return Response.
                        ok(create.readEntity(String.class)).
                        status(create.getStatus()).
                        build();
            }

            service = create.readEntity(Service.class);

            if (Logger.getLevel() == Level.FINE) {
                ObjectMapper mapper = new ObjectMapper();
                Logger.debug("ServiceResource.create: service: `%s`", mapper.writeValueAsString(service));
            }

            Response update = providerClient.enableService(cookie, service.id, provider.password);

            if (update.getStatus() >= 400) {
                return Response.
                        ok(update.readEntity(String.class)).
                        status(update.getStatus()).
                        build();
            }

            int u = jdbi.onDemand(ProvidersDAO.class)
                    .update(providerId, payload.url, service.auth, service.id, payload.name);

            if (u == 0) {
                Logger.warning("Failed to update Providers table with Service details");
            }

            _Result result = new _Result();
            result.auth = service.auth;
            result.code = String.format("%s:%s", providerId, service.id);
            result.key = token;
            result.url = payload.url;
            result.service = payload.name;

            Logger.info("ServiceResource.create: service authentication %s, code: %s", result.auth, result.code);

            return Response.
                    ok(result).
                    status(update.getStatus()).
                    build();
        } catch (JwtException e) {
            Logger.warning("ServiceResource.create %s", e);
            return Response.
                    ok(new ErrorMessage("Invalid Authorization token")).
                    status(401).
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

    @PUT
    @ApiOperation(value = "Update Service", response = _Result.class)
    public Response update(@ApiParam(hidden = true) @CookieParam("zroman") String token,
                           @ApiParam @Valid _UpdateService payload) {
        try {
            ProvidersDAO providersDAO = jdbi.onDemand(ProvidersDAO.class);

            if (token == null) {
                Response.
                        ok(new ErrorMessage("Not Authenticated")).
                        status(403).
                        build();
            }

            String subject = validateToken(token);

            Logger.debug("ServiceResource.update: provider: %s", subject);

            UUID providerId = UUID.fromString(subject);

            Provider provider = providersDAO.get(providerId);
            if (provider.serviceId == null) {
                return Response.
                        ok(new ErrorMessage("You have no service created yet")).
                        status(404).
                        build();
            }

            providersDAO.updateUrl(provider.id, payload.url);

            provider = providersDAO.get(providerId);

            _Result result = new _Result();
            result.key = token;
            result.auth = provider.serviceAuth;
            result.code = String.format("%s:%s", provider.id, provider.serviceId);
            result.url = provider.serviceUrl;

            return Response.
                    ok(result).
                    build();
        } catch (JwtException e) {
            Logger.warning("ServiceResource.update %s", e);
            return Response.
                    ok(new ErrorMessage("Invalid Authorization token")).
                    status(401).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ServiceResource.update: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get the Service", response = _Result.class)
    public Response get(@ApiParam(hidden = true) @CookieParam("zroman") String token) {
        try {
            if (token == null) {
                Response.
                        ok(new ErrorMessage("Not Authenticated")).
                        status(403).
                        build();
            }

            String subject = validateToken(token);

            Logger.debug("ServiceResource.get: provider: %s", subject);

            UUID providerId = UUID.fromString(subject);
            Provider provider = jdbi.onDemand(ProvidersDAO.class).get(providerId);

            _Result result = new _Result();
            result.key = token;
            result.auth = provider.serviceAuth;
            result.code = provider.serviceId != null ? String.format("%s:%s", provider.id, provider.serviceId) : null;
            result.url = provider.serviceUrl;
            result.email = provider.email;
            result.company = provider.name;
            result.service = provider.serviceName;

            return Response.
                    ok(result).
                    build();
        } catch (JwtException e) {
            Logger.warning("ServiceResource.get %s", e);
            return Response.
                    ok(new ErrorMessage("Invalid Authorization token")).
                    status(401).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ServiceResource.get: %s", e);
            return Response
                    .ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class _NewService {
        @NotNull
        @Length(min = 3, max = 128)
        @JsonProperty
        public String name;

        @JsonProperty
        public String url;

        @JsonProperty
        public String avatar;

        @ValidationMethod(message = "`url` is not a valid URL")
        @JsonIgnore
        public boolean isUrlValid() {
            if (url == null)
                return true;
            try {
                new URL(url);
                return url.contains(".");
            } catch (MalformedURLException e) {
                return false;
            }
        }

        @ValidationMethod(message = "`image` is not a Base64 encoded string")
        @JsonIgnore
        public boolean isAvatarValid() {
            return avatar == null || avatar.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class _UpdateService {
        @JsonProperty
        public String url;

        @ValidationMethod(message = "`url` is not a valid URL")
        @JsonIgnore
        public boolean isUrlValid() {
            if (url == null)
                return true;

            try {
                new URL(url);
                return url.contains(".");
            } catch (MalformedURLException e) {
                return false;
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _Result {
        @JsonProperty("service_code")
        @NotNull
        public String code;

        @JsonProperty("service_authentication")
        @NotNull
        public String auth;

        @JsonProperty("app_key")
        @NotNull
        public String key;

        @JsonProperty("webhook")
        public String url;

        @JsonProperty
        public String email;

        @JsonProperty
        public String company;

        @JsonProperty
        public String service;
    }
}