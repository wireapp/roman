package com.wire.bots.roman.filters;

import com.wire.bots.roman.Application;
import com.wire.bots.roman.Const;
import com.wire.lithium.server.monitoring.MDCUtils;
import com.wire.xenon.tools.Logger;
import io.jsonwebtoken.Jwts;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
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
            String subject = Jwts.parser()
                    .setSigningKey(Application.getKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

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
