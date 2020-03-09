package com.wire.bots.roman.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.OneOf;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncomingMessage {
    @NotNull
    @OneOf(value = {"text", "image", "poll.new", "poll.action.confirmation", "attachment"})
    @JsonProperty
    public String type;

    @JsonProperty
    public String text;
    @JsonProperty
    public String image;
    @JsonProperty
    public String attachment;
    @JsonProperty
    public Poll poll;

    @JsonIgnore
    @ValidationMethod(message = "`image` is not a Base64 encoded string")
    public boolean isValidImage() {
        if (!type.equals("image"))
            return true;
        return image != null && image.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
    }

    @JsonIgnore
    @ValidationMethod(message = "`attachment` is not a Base64 encoded string")
    public boolean isValidAttachment() {
        if (!type.equals("attachment"))
            return true;
        return attachment != null && attachment.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
    }

    @JsonIgnore
    @ValidationMethod(message = "`text` cannot be null")
    public boolean isValidText() {
        if (!type.equals("text"))
            return true;
        return text != null && !text.isEmpty();
    }

    @JsonIgnore
    @ValidationMethod(message = "`offset` & `userId` cannot be null")
    public boolean isValidPollActionConfirmation() {
        if (!type.equals("poll.action.confirmation"))
            return true;
        if (poll == null)
            return false;
        return poll.userId != null && poll.offset != null;
    }

    @JsonIgnore
    @ValidationMethod(message = "`poll` cannot be null")
    public boolean isValidPollNew() {
        if (!type.equals("poll.new"))
            return true;
        return poll != null;
    }
}
