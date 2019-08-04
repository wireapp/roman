package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alert {
    public Integer id;
    public String created;
    @NotNull
    public String title;
    @NotNull
    public String message;
    @NotNull
    public String category;
    @NotNull
    public Integer severity;
    @NotNull
    public UUID creator;
    public UUID contact;
    public String starting;
    public String ending;
    public Integer status;
    @NotNull
    public String responses;
}
