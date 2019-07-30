package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Template {
    public int id;
    public String created;
    public String title;
    public String message;
    public String category;
    public int severity;
    public UUID contact;
    public String responses;
}
