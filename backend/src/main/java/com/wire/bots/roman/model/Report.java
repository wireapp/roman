package com.wire.bots.roman.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wire.bots.roman.DAO.BroadcastDAO;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class Report {
    @JsonProperty
    @NotNull
    public UUID broadcastId;

    @JsonProperty
    @NotNull
    public List<BroadcastDAO.Pair> report;
}
