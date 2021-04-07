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

    public UUID messageId;
    public UUID conversationId;
    public String token;
    public String text;
    public String image;
    public String attachment;
    public String handle;
    public String locale;
    public Poll poll;
    public UUID refMessageId;
    public String mimeType;
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
