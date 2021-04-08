package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
    @JsonProperty
    @NotNull
    public String data;

    @JsonProperty
    public String filename;

    @JsonProperty
    @NotNull
    public String mimeType;

    @JsonProperty
    public Long duration;

    @JsonIgnore
    @ValidationMethod(message = "`data` is not a Base64 encoded string")
    public boolean isValidAttachment() {
        return data.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
    }

    @JsonIgnore
    @ValidationMethod(message = "Invalid `mimeType`")
    public boolean isValidMimeType() {
        return mimeType.contains("/");
    }
}
