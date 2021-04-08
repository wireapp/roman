package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
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

    public String text;
    public String image;
    public String attachment;
    public String mimeType;
    public Long duration;

    public Poll poll;
    public ArrayList<Mention> mentions = new ArrayList<>();
    public Call call;

    public void addMention(UUID userId, int offset, int len) {
        Mention mention = new Mention();
        mention.userId = userId;
        mention.offset = offset;
        mention.length = len;

        mentions.add(mention);
    }
}
