package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutgoingMessage {
    @NotNull
    public UUID botId;
    @NotNull
    public String type;
    @NotNull
    public UUID userId;
    public String handle;
    public String locale;

    public String token;

    public UUID messageId;
    public UUID refMessageId;
    public UUID conversationId;
    public String conversation;

    public Text text;
    public Attachment attachment;
    public Poll poll;
    public Call call;
    public String emoji;
}
