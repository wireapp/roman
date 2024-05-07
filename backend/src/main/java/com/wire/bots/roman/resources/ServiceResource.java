package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.*;
import com.wire.bots.roman.DAO.ProvidersDAO;
import com.wire.bots.roman.filters.ServiceAuthorization;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.model.Provider;
import com.wire.bots.roman.model.Service;
import com.wire.xenon.backend.models.ErrorMessage;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.tools.Logger;
import io.dropwizard.validation.ValidationMethod;
import io.swagger.annotations.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.hibernate.validator.constraints.Length;
import org.jdbi.v3.core.Jdbi;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static com.wire.bots.roman.Const.Z_PROVIDER;
import static com.wire.bots.roman.Const.Z_ROMAN;

@Api
@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource {
    private static final String PROFILE_KEY = "3-1-c9262f6f-892f-40d5-9349-fbeb62c8aba4";

    private final ProviderClient providerClient;
    private final ProvidersDAO providersDAO;

    public ServiceResource(Jdbi jdbi, ProviderClient providerClient) {
        this.providerClient = providerClient;
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
    }

    @POST
    @ApiOperation(value = "Create new service.", nickname = "createNewService")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = _ServiceInformation.class, message = "Service created."),
            @ApiResponse(code = 500, response = ErrorMessage.class, message = "Something went wrong."),
    })
    @ServiceAuthorization
    public Response create(@ApiParam(hidden = true) @CookieParam(Z_ROMAN) String token,
                           @Context ContainerRequestContext context,
                           @ApiParam @Valid _NewService payload) {
        try {
            //hack
            if (payload.url != null && payload.url.isEmpty()) {
                payload.url = null;
            }

            UUID providerId = (UUID) context.getProperty(Const.PROVIDER_ID);

            Provider provider = providersDAO.get(providerId);

            Logger.debug("ServiceResource.create: provider: %s, %s", provider.id, provider.email);

            Service service = newService();
            service.name = payload.name;
            service.summary = payload.summary;
            service.description = payload.description;

            NewCookie cookie;

            try (Response login = providerClient.login(provider.email, provider.password)) {
                Logger.debug("ServiceResource.create: login status: %d", login.getStatus());

                if (login.getStatus() >= 400) {
                    String msg = login.readEntity(String.class);
                    Logger.warning("ServiceResource.create: login response: %s", msg);
                    return Response.
                            ok(msg).
                            status(login.getStatus()).
                            build();
                }

                cookie = login.getCookies().get(Z_PROVIDER);

                if (payload.avatar != null) {
                    byte[] image = Base64.getDecoder().decode(payload.avatar);
                    if (image != null) {
                        String mimeType = "image/png";
                        Picture mediumImage = ImageProcessor.getMediumImage(new Picture(image, mimeType));
                        AssetKey assetKey = providerClient.uploadProfilePicture(cookie, mediumImage.getImageData(), mimeType);
                        service.assets.get(0).key = assetKey.id;
                        service.assets.get(1).key = assetKey.id;
                    }
                }
            }

            try (Response create = providerClient.createService(cookie, service)) {
                Logger.debug("ServiceResource.create: create service status: %d", create.getStatus());

                if (create.getStatus() >= 400) {
                    String msg = create.readEntity(String.class);
                    Logger.warning("ServiceResource.create: create service response: %s", msg);
                    return Response.
                            ok(msg).
                            status(create.getStatus()).
                            build();
                }

                service = create.readEntity(Service.class);
            }

            try (Response update = providerClient.enableService(cookie, service.id, provider.password)) {
                Logger.debug("ServiceResource.create: enable service status: %d", update.getStatus());

                if (update.getStatus() >= 400) {
                    String msg = update.readEntity(String.class);
                    Logger.warning("ServiceResource.create: enable service (%s) response: %s", service.id, msg);
                    return Response.
                            ok(msg).
                            status(update.getStatus()).
                            build();
                }

                providersDAO.update(providerId, payload.url, service.auth, service.id, payload.name, payload.commandPrefix);

                provider = providersDAO.get(providerId);

                _ServiceInformation result = new _ServiceInformation();
                result.auth = provider.serviceAuth;
                result.key = token;
                result.code = String.format("%s:%s", providerId, provider.serviceId);
                result.url = provider.serviceUrl;
                result.service = provider.serviceName;
                result.company = provider.name;
                result.commandPrefix = provider.commandPrefix;

                Logger.info("ServiceResource.create: service authentication %s, code: %s", result.auth, result.code);

                return Response.
                        ok(result).
                        status(update.getStatus()).
                        build();
            }
        } catch (Exception e) {
            Logger.exception(e, "ServiceResource.create: %s", e.getMessage());
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @PUT
    @ApiOperation(value = "Update Service", response = _ServiceInformation.class, nickname = "updateService")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = _ServiceInformation.class, message = "Service updated."),
            @ApiResponse(code = 500, response = ErrorMessage.class, message = "Something went wrong."),
    })
    @ServiceAuthorization
    public Response update(@Context ContainerRequestContext context,
                           @ApiParam(hidden = true) @CookieParam(Z_ROMAN) String token,
                           @ApiParam @Valid _UpdateService payload) {
        try {
            final UUID providerId = (UUID) context.getProperty("providerid");

            Provider provider = providersDAO.get(providerId);
            if (provider.serviceId == null) {
                return Response.
                        ok(new ErrorMessage("You have no service created yet")).
                        status(404).
                        build();
            }

            if (payload.url != null) {
                String url = payload.url.equals("null") ? null : payload.url;
                providersDAO.updateUrl(provider.id, url);
            }

            Response login = providerClient.login(provider.email, provider.password);

            Logger.debug("ServiceResource.update: login status: %d", login.getStatus());

            if (login.getStatus() >= 400) {
                return Response.
                        ok(login.readEntity(String.class)).
                        status(login.getStatus()).
                        build();
            }

            NewCookie cookie = login.getCookies().get(Z_PROVIDER);

            if (payload.name != null) {
                providersDAO.updateServiceName(provider.id, payload.name);
                providerClient.updateServiceName(cookie, provider.serviceId, payload.name);
            }

            if (payload.commandPrefix != null) {
                providersDAO.updateServicePrefix(provider.id, payload.commandPrefix);
            }

            if (payload.avatar != null) {
                byte[] image = Base64.getDecoder().decode(payload.avatar);
                String mimeType = "image/jpeg";
                Picture mediumImage = ImageProcessor.getMediumImage(new Picture(image, mimeType));
                AssetKey assetKey = providerClient.uploadProfilePicture(cookie, mediumImage.getImageData(), mimeType);
                providerClient.updateServiceAvatar(cookie, provider.serviceId, assetKey.id);
            }

            provider = providersDAO.get(providerId);

            _ServiceInformation result = new _ServiceInformation();
            result.key = token;
            result.auth = provider.serviceAuth;
            result.code = String.format("%s:%s", provider.id, provider.serviceId);
            result.url = provider.serviceUrl;
            result.service = provider.serviceName;
            result.company = provider.name;
            result.commandPrefix = provider.commandPrefix;

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.exception(e, "ServiceResource.update: %s", e.getMessage());
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @GET
    @ApiOperation(value = "Get the Service", nickname = "getService")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = _ServiceInformation.class, message = "Service."),
            @ApiResponse(code = 500, response = ErrorMessage.class, message = "Something went wrong."),
    })
    @ServiceAuthorization
    public Response get(@ApiParam(hidden = true) @CookieParam(Z_ROMAN) String token,
                        @Context ContainerRequestContext context) {
        try {
            final UUID providerId = (UUID) context.getProperty(Const.PROVIDER_ID);

            Logger.debug("ServiceResource.get: provider: %s", providerId);

            Provider provider = providersDAO.get(providerId);

            _ServiceInformation result = new _ServiceInformation();
            result.key = token;
            result.auth = provider.serviceAuth;
            result.code = String.format("%s:%s", provider.id, provider.serviceId);
            result.url = provider.serviceUrl;
            result.email = provider.email;
            result.company = provider.name;
            result.service = provider.serviceName;
            result.commandPrefix = provider.commandPrefix;

            return Response.
                    ok(result).
                    build();
        } catch (Exception e) {
            Logger.exception(e, "ServiceResource.get: %s", e.getMessage());
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    @DELETE
    @ApiOperation(value = "Delete the Service", nickname = "deleteService")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Service created."),
            @ApiResponse(code = 500, response = ErrorMessage.class, message = "Something went wrong."),
    })
    @ServiceAuthorization
    public Response delete(@ApiParam(hidden = true) @CookieParam(Z_ROMAN) String token,
                           @Context ContainerRequestContext context) {
        try {
            final UUID providerId = (UUID) context.getProperty(Const.PROVIDER_ID);

            Logger.debug("ServiceResource.delete: provider: %s", providerId);

            Provider provider = providersDAO.get(providerId);

            providersDAO.deleteService(providerId);

            try (Response login = providerClient.login(provider.email, provider.password)) {

                NewCookie cookie = login.getCookies().get(Z_PROVIDER);

                return providerClient.deleteService(cookie, provider.serviceId);
            }
        } catch (Exception e) {
            Logger.exception(e, "ServiceResource.delete: %s", e.getMessage());
            return Response
                    .ok(new ErrorMessage("Something went wrong"))
                    .status(500)
                    .build();
        }
    }

    private Service newService() {
        final Config config = Application.getInstance().getConfig();
        Service ret = new Service();
        ret.baseUrl = config.domain;
        ret.pubkey = Tools.decodeBase64(config.romanPubKeyBase64);

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
        @Length(min = 3, max = 128)
        public String summary = "Summary";

        @JsonProperty
        @Length(min = 3, max = 128)
        public String description = "Powered by Roman";

        @JsonProperty("command_prefix")
        public String commandPrefix;

        @ValidationMethod(message = "`url` is not a valid URL")
        @JsonIgnore
        public boolean isUrlValid() {
            if (url == null || url.isEmpty())
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
            return avatar == null
                    || (!avatar.isEmpty() && avatar.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$"));
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

        @JsonProperty("command_prefix")
        public String commandPrefix;

        @ValidationMethod(message = "`url` is not a valid URL")
        @JsonIgnore
        public boolean isUrlValid() {
            if (url == null || Objects.equals(url, "null"))
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
    static class _ServiceInformation {
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

        @JsonProperty("command_prefix")
        public String commandPrefix;
    }
}
