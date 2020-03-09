package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutgoingMessage {
    @NotNull
    public UUID botId;

    @NotNull
    public UUID userId;

    public UUID messageId;

    @NotNull
    public String type;

    public String token;

    public String text;
    public String image;
    public String handle;
    public String locale;
    public Poll poll;
    public UUID refMessageId;
}
