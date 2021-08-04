package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignIn {
    @NotNull
    @Length(min = 8)
    public String email;

    @NotNull
    @Length(min = 6, max = 24)
    public String password;
}
