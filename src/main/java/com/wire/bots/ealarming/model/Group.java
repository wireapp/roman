package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
