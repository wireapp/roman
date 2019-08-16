package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertPayload {
    @NotNull
    public String title;

    @NotNull
    public String message;

    @NotNull
    @Max(4)
    @Min(1)
    public Integer severity;

    @NotNull
    public List<String> responses;

    @NotNull
    public List<Integer> groups;

    @NotNull
    public List<UUID> exclude;

    @NotNull
    public List<UUID> include;

}
