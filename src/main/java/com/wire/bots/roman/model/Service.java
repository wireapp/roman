package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {
    @NotNull
    public String name;

    @NotNull
    @JsonProperty("base_url")
    public String baseUrl;

    public String description = "Description";

    public String summary = "Summary";

    @JsonProperty("public_key")
    public String pubkey;

    public String[] tags = new String[]{"tutorial"};

}
