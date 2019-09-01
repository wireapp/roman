package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Provider {
    @NotNull
    public String email;

    @NotNull
    public String password;

    @NotNull
    public String name;

    public String url = "https://services.wire.com";

    public String description = "Description";
}
