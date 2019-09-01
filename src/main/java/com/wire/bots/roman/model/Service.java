package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {
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

    @JsonProperty("auth_token")
    public String token;

    public String[] tags;

}
