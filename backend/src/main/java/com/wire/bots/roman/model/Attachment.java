package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
    @JsonProperty
    public String data;

    @JsonProperty
    public String name;

    @JsonProperty
    @NotNull
    public String mimeType;

    @JsonProperty
    @NotNull
    public Long size;

    @JsonProperty
    public Long duration;

    @JsonProperty
    public byte[] levels;

    @JsonProperty
    public Integer height;

    @JsonProperty
    public Integer width;

    @JsonProperty
    public AssetMeta meta;

    @JsonIgnore
    @ValidationMethod(message = "Invalid `mimeType`")
    public boolean isValidMimeType() {
        return mimeType.contains("/");
    }
}
