package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;

public class Text {
    @JsonProperty
    @NotNull
    @Length(min = 1, max = 64000)
    public String data;

    @JsonProperty
    public ArrayList<Mention> mentions;
}
