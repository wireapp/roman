package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageIn {
    public UUID botId;
    public UUID convId;
    public UUID from;
    public String type;
    public String text;
    public String token;
}
