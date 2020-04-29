package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.Application;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.ImageProcessor;
import com.wire.bots.roman.ProviderClient;
import com.wire.bots.roman.Tools;
import com.wire.bots.roman.filters.ServiceAuthorization;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.Service;
import com.wire.bots.sdk.assets.Picture;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.tools.Logger;
import io.dropwizard.validation.ValidationMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Length;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

@Api
@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {
    private static final String PROFILE_KEY = "3-1-c9262f6f-892f-40d5-9349-fbeb62c8aba4";

    private final ProviderClient providerClient;
    private final ProvidersDAO providersDAO;

    public ServiceResource(DBI jdbi, ProviderClient providerClient) {
        this.providerClient = providerClient;
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
    }

    @POST
    @ApiOperation(value = "Create new Service", response = _Result.class)
    @ServiceAuthorization
    public Response create(@ApiParam(hidden = true) @CookieParam("zroman") String token,
                           @Context ContainerRequestContext context,
                           @ApiParam @Valid _NewService payload) {
        try {
            UUID providerId = (UUID) context.getProperty("providerid");

            Provider provider = providersDAO.get(providerId);

            Logger.debug("ServiceResource.create: provider: %s, %s", provider.id, provider.email);

            Response login = providerClient.login(provider.email, provider.password);

            Logger.debug("ServiceResource.create: login status: %d", login.getStatus());

            if (login.getStatus() >= 400) {
                return Response.
                        ok(login.readEntity(String.class)).
                        status(login.getStatus()).
                        build();
            }

            NewCookie cookie = login.getCookies().get("zprovider");

            Service service = newService();
            service.name = payload.name;
            service.summary = payload.summary;

            if (payload.avatar != null) {
                byte[] image = Base64.getDecoder().decode(payload.avatar);
                Picture mediumImage = ImageProcessor.getMediumImage(new Picture(image));
                String key = providerClient.uploadProfilePicture(cookie, mediumImage.getImageData(), mediumImage.getMimeType());
                service.assets.get(0).key = key;
                service.assets.get(1).key = key;
            }

            Response create = providerClient.createService(cookie, service);

            Logger.debug("ServiceResource.create: create service status: %d", create.getStatus());

            if (create.getStatus() >= 400) {
                return Response.
                        ok(create.readEntity(String.class)).
                        status(create.getStatus()).
                        build();
            }

            service = create.readEntity(Service.class);

            Response update = providerClient.enableService(cookie, service.id, provider.password);

            Logger.debug("ServiceResource.create: enable service status: %d", update.getStatus());

            if (update.getStatus() >= 400) {
                return Response.
                        ok(update.readEntity(String.class)).
                        status(update.getStatus()).
                        build();
            }

            providersDAO.update(providerId, payload.url, service.auth, service.id, payload.name);

            provider = providersDAO.get(providerId);

            _Result result = new _Result();
            result.auth = provider.serviceAuth;
            result.key = token;
            result.code = String.format("%s:%s", providerId, provider.serviceId);
            result.url = provider.serviceUrl;
            result.service = provider.serviceName;

            Logger.info("ServiceResource.create: service authentication %s, code: %s", result.auth, result.code);

            return Response.
                    ok(result).
                    status(update.getStatus()).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ServiceResource.create: %s", e);
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @PUT
    @ApiOperation(value = "Update Service", response = _Result.class)
    @ServiceAuthorization
    public Response update(@Context ContainerRequestContext context,
                           @ApiParam(hidden = true) @CookieParam("zroman") String token,
                           @ApiParam @Valid _UpdateService payload) {
        try {
            UUID providerId = (UUID) context.getProperty("providerid");

            Provider provider = providersDAO.get(providerId);
            if (provider.serviceId == null) {
                return Response.
                        ok(new ErrorMessage("You have no service created yet")).
                        status(404).
                        build();
            }

            if (payload.url != null) {
                providersDAO.updateUrl(provider.id, payload.url);
            }

            Response login = providerClient.login(provider.email, provider.password);

            Logger.debug("ServiceResource.create: login status: %d", login.getStatus());

            if (login.getStatus() >= 400) {
                return Response.
                        ok(login.readEntity(String.class)).
                        status(login.getStatus()).
                        build();
            }

            NewCookie cookie = login.getCookies().get("zprovider");

            if (payload.name != null) {
                providersDAO.updateServiceName(provider.id, payload.name);
                providerClient.updateServiceName(cookie, provider.serviceId, payload.name);
            }

            if (payload.avatar != null) {
                byte[] image = Base64.getDecoder().decode(payload.avatar);
                Picture mediumImage = ImageProcessor.getMediumImage(new Picture(image));
                String key = providerClient.uploadProfilePicture(cookie, mediumImage.getImageData(), mediumImage.getMimeType());
                providerClient.updateServiceAvatar(cookie, provider.serviceId, key);
            }

            provider = providersDAO.get(providerId);

            _Result result = new _Result();
            result.key = token;
            result.auth = provider.serviceAuth;
            result.code = String.format("%s:%s", provider.id, provider.serviceId);
            result.url = provider.serviceUrl;
            result.service = provider.serviceName;

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ServiceResource.update: %s", e);
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get the Service", response = _Result.class)
    @ServiceAuthorization
    public Response get(@ApiParam(hidden = true) @CookieParam("zroman") String token,
                        @Context ContainerRequestContext context) {
        try {
            UUID providerId = (UUID) context.getProperty("providerid");

            Logger.debug("ServiceResource.get: provider: %s", providerId);

            Provider provider = providersDAO.get(providerId);

            _Result result = new _Result();
            result.key = token;
            result.auth = provider.serviceAuth;
            result.code = String.format("%s:%s", provider.id, provider.serviceId);
            result.url = provider.serviceUrl;
            result.email = provider.email;
            result.company = provider.name;
            result.service = provider.serviceName;

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("ServiceResource.get: %s", e);
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    private Service newService() throws IOException {
        final String domain = Application.getInstance().getConfig().domain;

        Service ret = new Service();
        ret.baseUrl = domain;
        ret.pubkey = Tools.getPubkey(domain);

        ret.assets = new ArrayList<>();
        Service._Asset asset1 = new Service._Asset();
        asset1.key = PROFILE_KEY;
        asset1.size = "complete";
        ret.assets.add(asset1);

        Service._Asset asset2 = new Service._Asset();
        asset2.key = PROFILE_KEY;
        asset2.size = "preview";
        ret.assets.add(asset2);

        return ret;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class _NewService {
        @NotNull
        @Length(min = 3, max = 64)
        @JsonProperty
        public String name;

        @JsonProperty
        public String url;

        @JsonProperty
        public String avatar;

        @JsonProperty
        @NotNull
        @Length(min = 3, max = 128)
        public String summary;

        @ValidationMethod(message = "`url` is not a valid URL")
        @JsonIgnore
        public boolean isUrlValid() {
            if (url == null)
                return true;
            try {
                new URL(url).toURI();
                return true;
            } catch (URISyntaxException | MalformedURLException e) {
                return false;
            }
        }

        @ValidationMethod(message = "`avatar` is not a Base64 encoded string")
        @JsonIgnore
        public boolean isAvatarValid() {
            return avatar == null || avatar.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class _UpdateService {
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
                new URL(url).toURI();
                return true;
            } catch (URISyntaxException | MalformedURLException e) {
                return false;
            }
        }

        @ValidationMethod(message = "`avatar` is not a Base64 encoded string")
        @JsonIgnore
        public boolean isAvatarValid() {
            return avatar == null || avatar.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
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