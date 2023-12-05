package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {
    public UUID id;

    @NotNull
    public String name;

    @JsonProperty("auth_token")
    public String auth;

    @NotNull
    @JsonProperty("base_url")
    public String baseUrl;

    @JsonProperty
    public String description = "Powered by Roman";

    @JsonProperty
    public String summary = "Summary";

    @JsonProperty("public_key")
    public String pubkey;
    @JsonProperty
    public ArrayList<_Asset> assets;

    public String[] tags = new String[]{"tutorial"};

    public static class _Asset {
        @JsonProperty
        public String type = "image";

        @JsonProperty
        public String key;

        @JsonProperty
        public String size;
    }
}
