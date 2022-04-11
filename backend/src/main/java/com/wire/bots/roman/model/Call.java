package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
    public String sequence;

    @JsonProperty("secret")
    public String secret;

    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("conf_id")
    public String confId;

    @JsonProperty("src_clientid")
    public String clientId;

    @JsonProperty("src_userid")
    public UUID userId;

    @JsonProperty
    public ArrayList<PreKey> keys;

    static class PreKey {
        @JsonProperty
        @NotNull
        public int idx;

        @JsonProperty
        @NotNull
        public String data;   //base64 encoded data
    }
}
