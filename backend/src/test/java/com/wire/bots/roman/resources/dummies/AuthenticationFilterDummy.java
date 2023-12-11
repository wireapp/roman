package com.wire.bots.roman.resources.dummies;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

public class AuthenticationFilterDummy implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty("botid", Const.BOT_ID);
    }
}
