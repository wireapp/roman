package com.wire.bots.roman.filters;

import com.wire.bots.roman.Const;
import com.wire.bots.roman.Tools;
import com.wire.lithium.server.monitoring.MDCUtils;
import com.wire.xenon.tools.Logger;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.UUID;

@Provider
public class ServiceTokenAuthenticationFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String token = requestContext.getHeaderString(Const.APP_KEY);

        if (token == null) {
            Logger.info("ServiceTokenAuthenticationFilter: missing token");
            Exception cause = new IllegalArgumentException("Missing Authorization");
            throw new WebApplicationException(cause, Response.Status.UNAUTHORIZED);
        }

        try {
            String subject = Tools.validateToken(token);

            UUID providerId = UUID.fromString(subject);
            requestContext.setProperty(Const.PROVIDER_ID, providerId);
            MDCUtils.put("providerId", providerId);
        } catch (Exception e) {
            Logger.info("ServiceTokenAuthenticationFilter: %s %s %s", token, e, e.getMessage());
            Exception cause = new IllegalArgumentException(e.getMessage());
            throw new WebApplicationException(cause, Response.Status.UNAUTHORIZED);
        }
    }

    @Provider
    public static class ServiceTokenAuthenticationFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            if (resourceInfo.getResourceMethod().getAnnotation(ServiceTokenAuthorization.class) != null) {
                context.register(ServiceTokenAuthenticationFilter.class);
            }
        }
    }
}
