package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertPayload {
    @NotNull
    public Alert alert;
    @NotNull
    public ArrayList<Integer> groups;
    @NotNull
    public ArrayList<UUID> exclude;

    @NotNull
    public ArrayList<UUID> include;

}
