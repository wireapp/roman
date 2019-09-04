package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.dropwizard.validation.OneOf;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingMessage {
    @NotNull
    @OneOf(value = {"text", "image"}, ignoreCase = true, ignoreWhitespace = true)
    public String type;
    public String text;
    public String image;

    @ValidationMethod(message = "`Image` is not a Base64 encoded string")
    @JsonIgnore
    public boolean isImage() {
        return image == null || image.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
    }
}
