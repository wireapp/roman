package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
    public Integer id;
    public String created; //time of creation
    public UUID contact;

    @NotNull
    public String title;

    @NotNull
    public String message;

    @NotNull
    @Max(3)
    @Min(0)
    public Integer severity;

    @NotNull
    public List<String> responses;

}
