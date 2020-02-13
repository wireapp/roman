package com.wire.bots.roman.test.resources.dummies;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import static com.wire.bots.roman.test.resources.dummies.Const.BOT_ID;

public class AuthenticationFilterDummy implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty("botid", BOT_ID);
    }
}