package com.wire.bots.roman.filters;

import io.swagger.annotations.Authorization;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Objects;

import static com.wire.bots.roman.Const.WIRE_AUTH;

@Provider
public class BackendAuthenticationFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (auth == null) {
            Exception cause = new IllegalArgumentException("Missing Authorization");
            throw new WebApplicationException(cause, Response.Status.UNAUTHORIZED);
        }

        String[] split = auth.split(" ");

        if (split.length != 2) {
            Exception cause = new IllegalArgumentException("Bad Authorization");
            throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
        }

        String type = split[0];
        String token = split[1];

        if (!Objects.equals(type, "Bearer")) {
            Exception cause = new IllegalArgumentException("Bad Authorization");
            throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
        }

        requestContext.setProperty(WIRE_AUTH, token);
    }

    @Provider
    public static class BackendAuthenticationFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            if (resourceInfo.getResourceMethod().getAnnotation(Authorization.class) != null) {
                context.register(BackendAuthenticationFilter.class);
            }
        }
    }
}