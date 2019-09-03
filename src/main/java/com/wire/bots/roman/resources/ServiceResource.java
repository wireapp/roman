package com.wire.bots.roman.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.hibernate.validator.constraints.NotEmpty;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
@Path("/provider/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {

    private final ProvidersDAO providersDAO;
    private final ProviderClient providerClient;

    public ServiceResource(DBI jdbi, ProviderClient providerClient) {
        providersDAO = jdbi.onDemand(ProvidersDAO.class);
        this.providerClient = providerClient;
    }

    @POST
    @ApiOperation(value = "Register new Service", response = _Result.class)
    public Response create(@ApiParam(hidden = true) @NotNull @CookieParam("zroman") String token,
                           @ApiParam @Valid _NewService payload) {
        try {
            String subject = validateToken(token);

            Logger.debug("ServiceResource.create: provider: %s", subject);

            UUID providerId = UUID.fromString(subject);
            Provider provider = providersDAO.get(providerId);

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

            providersDAO.add(providerId, payload.url, service.auth);

            _Result result = new _Result();
            result.auth = service.auth;
            result.code = String.format("%s:%s", providerId, service.id);

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



    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _NewService {
        @NotNull
        @NotEmpty
        @JsonProperty
        public String name;

        @JsonProperty
        @NotNull
        public String url;

        @JsonProperty
        public String avatar;

        @ValidationMethod(message = "Malformed URL")
        @JsonIgnore
        public boolean isUrl() {
            try {
                new URL(url);
                if (!url.contains("."))
                    return false;
                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        }
    }

    static class _Result {
        @JsonProperty("service_code")
        @NotNull
        public String code;

        @JsonProperty("service_authentication")
        @NotNull
        public String auth;
    }
}