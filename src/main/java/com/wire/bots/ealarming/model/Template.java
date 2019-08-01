package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
    public Integer id;
    public String created;
    public String title;
    public String message;
    public String category;
    public Integer severity;
    public UUID contact;
    public String responses;
}
