package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert2User {
    public int alertId;
    public UUID userId;
    public Integer messageStatus;
    public String escalated;
    public Integer responseId;
}
