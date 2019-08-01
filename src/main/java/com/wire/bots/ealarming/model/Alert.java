package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alert {
    public Integer id;
    public String created;
    public String title;
    public String message;
    public String category;
    public Integer severity;
    public UUID creator;
    public UUID contact;
    public String starting;
    public String ending;
    public Integer status;
    public String responses;
}
