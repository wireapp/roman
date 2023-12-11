package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Provider {
    public UUID id;
    public String name;

    @NotNull
    public String email;

    @NotNull
    public String password;

    public String hash;

    public String serviceUrl;

    public String serviceAuth;

    public UUID serviceId;

    public String serviceName;

    public String commandPrefix;
}
