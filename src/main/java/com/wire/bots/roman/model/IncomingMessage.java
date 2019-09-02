package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingMessage {
    @NotNull
    public String type;
    public String text;
    public String image;
}
