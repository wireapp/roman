package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alert2User {
    public enum Type {
        SCHEDULED,
        SENT,
        DELIVERED,
        READ,
        RESPONDED
    }

    public Integer alertId;
    public UUID userId;
    public Type status;
    public UUID messageId;
    public String response;
    public Integer escalated;
    public String created;
}
