package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertResult {
    public Alert alert;
    public List<Group> groups;
    public List<Alert2User> users;
}
