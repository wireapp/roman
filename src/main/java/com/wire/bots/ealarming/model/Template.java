package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
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
    public UUID contact;
    @NotNull
    public String responses;
    @NotNull
    public ArrayList<Integer> groups;
}
