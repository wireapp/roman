package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Poll {
    @NotNull
    public UUID id;

    public String body;
    public ArrayList<String> buttons;

    public Integer offset;
    public UUID userId;
}
