package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.OneOf;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingMessage {
    @NotNull
    @OneOf(value = {"text", "image"})
    @JsonProperty
    public String type;

    @JsonProperty
    public String text;
    @JsonProperty
    public String image;

    @JsonIgnore
    @ValidationMethod(message = "`image` is not a Base64 encoded string")
    public boolean isValidImage() {
        if (!type.equals("image"))
            return true;
        return image != null && image.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
    }

    @JsonIgnore
    @ValidationMethod(message = "`text` cannot be null")
    public boolean isValidText() {
        if (!type.equals("text"))
            return true;
        return text != null && !text.isEmpty();
    }
}
