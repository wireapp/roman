package com.wire.bots.roman.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public class Mention {
    @NotNull
    public UUID userId;
    @NotNull
    @Min(0)
    public Integer offset;
    @NotNull
    @Min(2)
    public Integer length;
}
