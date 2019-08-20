package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    @NotNull
    public Integer id;
    @NotNull
    public String name;
    @NotNull
    public Integer type;
    @JsonIgnore
    public Integer deleted;
    @NotNull
    public Integer size;

}
