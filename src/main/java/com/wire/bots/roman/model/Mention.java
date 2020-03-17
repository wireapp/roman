package com.wire.bots.roman.model;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class Mention {
    @NotNull
    public UUID userId;
    @NotNull
    public Integer offset;
    @NotNull
    public Integer length;
}
