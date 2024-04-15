package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetMeta {
    @JsonProperty
    @NotNull
    public String assetId;

    @JsonProperty
    public String assetToken;

    @JsonProperty
    @NotNull
    public String sha256;

    @JsonProperty
    @NotNull
    public String otrKey;

    @JsonProperty
    @NotNull
    public String domain;
}
