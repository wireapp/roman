package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.OneOf;
import io.dropwizard.validation.ValidationMethod;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Poll {
    @NotNull
    public UUID id;

    @NotNull
    @OneOf(value = {"create", "confirmation", "new"})
    @JsonProperty
    public String type;

    public ArrayList<String> buttons;

    public Integer offset;
    public UUID userId;

    @JsonIgnore
    @ValidationMethod(message = "`offset` & `userId` cannot be null")
    public boolean isValidConfirmation() {
        if (!type.equals("confirmation"))
            return true;

        return userId != null && offset != null;
    }
}
