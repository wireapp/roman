package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.Tools;
import com.wire.bots.sdk.tools.Util;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {

    private static final String PROFILE_KEY = "3-1-c9262f6f-892f-40d5-9349-fbeb62c8aba4";
    @JsonProperty("auth_token")
    public String auth;

    public UUID id;

    @NotNull
    public String name;

    @NotNull
    @JsonProperty("base_url")
    public String baseUrl;

    public String description;

    public String summary;

    @JsonProperty("public_key")
    public String pubkey;
    @JsonProperty
    public ArrayList<_Asset> assets;

    public String[] tags;

    public Service() throws IOException {
        summary = "Summary";
        description = "Description";
        tags = new String[]{"tutorial"};
        baseUrl = String.format("https://services.%s/roman", Util.getDomain());
        pubkey = String.format("%s\n%s\n%s", "-----BEGIN PUBLIC KEY-----", Tools.getPubkey(), "-----END PUBLIC KEY-----");

        assets = new ArrayList<>();
        _Asset asset1 = new _Asset();
        asset1.key = PROFILE_KEY;
        asset1.size = "complete";
        assets.add(asset1);

        _Asset asset2 = new _Asset();
        asset2.key = PROFILE_KEY;
        asset2.size = "preview";
        assets.add(asset2);
    }

    public static class _Asset {
        @JsonProperty
        public String type = "image";

        @JsonProperty
        public String key;

        @JsonProperty
        public String size;
    }
}
