package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertResult {
    @NotNull
    public Alert alert;
    @NotNull
    public List<Group> groups;
}
