package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Call {
    @JsonProperty
    public String version = "3.0";

    @JsonProperty
    public String type;

    @JsonProperty("resp")
    public boolean response;

    @JsonProperty("sessid")
    public String sessionId;

    @JsonProperty("props")
    public HashMap<String, String> properties;

    // SFT
    @JsonProperty("sft_url")
    public String sftUrl;

    @JsonProperty("seqno")
    public int sequence;

    @JsonProperty("secret")
    public String secret;

    @JsonProperty("timestamp")
    public int timestamp;

    @JsonProperty("clientid")
    public String clientId;
}
