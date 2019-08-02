package com.wire.bots.ealarming.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Report {
    public int scheduled;
    public int sent;
    public int delivered;
    public int read;
    public int responded;
}
