package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BroadcastMessage {
    @JsonProperty
    @NotNull
    public String mimeType;

    @JsonProperty
    public String filename;

    @JsonProperty
    public Long duration;

    @JsonProperty
    public int size;

    @JsonProperty
    public byte[] levels;

    @JsonProperty
    public String assetKey;

    @JsonProperty
    public String assetToken;

    @JsonProperty
    public String sha256;

    @JsonProperty
    public String otrKey;

    @JsonProperty
    public ArrayList<Mention> mentions;
}
