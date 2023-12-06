package com.wire.bots.roman;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.model.Service;
import com.wire.bots.roman.model.SignIn;
import com.wire.xenon.models.AssetKey;
import com.wire.xenon.tools.Logger;
import com.wire.xenon.tools.Util;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class ProviderClient {
    private final WebTarget servicesTarget;
    private final WebTarget providerTarget;

    public ProviderClient(Client jerseyClient, String apiHost) {
        providerTarget = jerseyClient.target(apiHost)
                .path("provider");
        servicesTarget = providerTarget
                .path("services");
    }

    public Response register(String name, String email) {
        _NewProvider newProvider = new _NewProvider();
        newProvider.name = name;
        newProvider.email = email;
        newProvider.description = "Description";
        newProvider.url = "https://wire.com";

        return providerTarget.path("register")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(newProvider, MediaType.APPLICATION_JSON));
    }

    public Response login(String email, String password) {
        SignIn signIn = new SignIn();
        signIn.email = email;
        signIn.password = password;

        return providerTarget.path("login")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(signIn, MediaType.APPLICATION_JSON));
    }

    public Response createService(NewCookie zprovider, Service service) {
        return servicesTarget
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .post(Entity.entity(service, MediaType.APPLICATION_JSON));
    }

    public Response deleteService(NewCookie zprovider, UUID serviceId) {
        return servicesTarget
                .path(serviceId.toString())
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .delete();
    }

    public Response enableService(NewCookie zprovider, UUID serviceId, String password) {
        _UpdateService updateService = new _UpdateService();
        updateService.enabled = true;
        updateService.password = password;

        WebTarget connection = servicesTarget
                .path(serviceId.toString())
                .path("connection");
        Logger.debug("enableService: PUT %s", connection.getUri());
        return connection
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));
    }

    public Response updateServiceName(NewCookie zprovider, UUID serviceId, String name) {
        _UpdateService updateService = new _UpdateService();
        updateService.name = name;

        return servicesTarget
                .path(serviceId.toString())
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));
    }

    public Response updateServiceAvatar(NewCookie zprovider, UUID serviceId, String key) {
        _UpdateService updateService = new _UpdateService();

        ArrayList<_UpdateService._Asset> assets = new ArrayList<>();
        _UpdateService._Asset asset1 = new _UpdateService._Asset();
        asset1.key = key;
        asset1.size = "complete";
        assets.add(asset1);

        _UpdateService._Asset asset2 = new _UpdateService._Asset();
        asset2.key = key;
        asset2.size = "preview";
        assets.add(asset2);

        updateService.assets = assets;

        return servicesTarget
                .path(serviceId.toString())
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));
    }

    public Response updateServicePubKey(NewCookie zprovider, UUID serviceId, String password, String pubkey) {
        _UpdateService updateService = new _UpdateService();
        updateService.pubKeys = new String[]{pubkey};
        updateService.password = password;

        return servicesTarget
                .path(serviceId.toString())
                .path("connection")
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));
    }

    public Response updateServiceURL(NewCookie zprovider, UUID serviceId, String password, String url) {
        _UpdateService updateService = new _UpdateService();
        updateService.baseUrl = url;
        updateService.password = password;

        return servicesTarget
                .path(serviceId.toString())
                .path("connection")
                .request(MediaType.APPLICATION_JSON)
                .cookie(zprovider)
                .put(Entity.entity(updateService, MediaType.APPLICATION_JSON));
    }

    public String uploadProfilePicture(Cookie cookie, byte[] image, String mimeType) throws Exception {
        final boolean isPublic = true;
        final String retention = "eternal";
        String strMetadata = String.format("{\"public\": %s, \"retention\": \"%s\"}", isPublic, retention);
        StringBuilder sb = new StringBuilder();

        // Part 1
        sb.append("--frontier\r\n");
        sb.append("Content-Type: application/json; charset=utf-8\r\n");
        sb.append("Content-Length: ")
                .append(strMetadata.length())
                .append("\r\n\r\n");
        sb.append(strMetadata)
                .append("\r\n");

        // Part 2
        sb.append("--frontier\r\n");
        sb.append("Content-Type: ")
                .append(mimeType)
                .append("\r\n");
        sb.append("Content-Length: ")
                .append(image.length)
                .append("\r\n");
        sb.append("Content-MD5: ")
                .append(Util.calcMd5(image))
                .append("\r\n\r\n");

        // Complete
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        os.write(image);
        os.write("\r\n--frontier--\r\n".getBytes(StandardCharsets.UTF_8));

        Response response = providerTarget
                .path("assets")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .cookie(cookie)
                .post(Entity.entity(os.toByteArray(), "multipart/mixed; boundary=frontier"));

        if (response.getStatus() >= 400) {
            Logger.warning(response.readEntity(String.class));
            return null;
        }

        AssetKey assetKey = response.readEntity(AssetKey.class);

        return assetKey.id;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class _UpdateService {
        @JsonProperty
        public String password;

        @JsonProperty
        public String name;

        @JsonProperty
        public boolean enabled;

        @JsonProperty("public_keys")
        public String[] pubKeys;

        @JsonProperty("base_url")
        public String baseUrl;

        @JsonProperty
        public ArrayList<_Asset> assets;

        public static class _Asset {
            @JsonProperty
            public String type = "image";

            @JsonProperty
            public String key;

            @JsonProperty
            public String size;
        }
    }


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
