package com.wire.bots.roman.filters;

import com.wire.bots.roman.Application;
import io.jsonwebtoken.Jwts;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.UUID;

@Provider
public class ServiceAuthenticationFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        Cookie authCookie = requestContext.getCookies().get("zroman");

        if (authCookie == null) {
            Exception cause = new IllegalArgumentException("Missing Authorization");
            throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
        }

        String token = authCookie.getValue();

        try {
            String subject = Jwts.parser()
                    .setSigningKey(Application.getKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            UUID providerId = UUID.fromString(subject);
            requestContext.setProperty("providerid", providerId);
        } catch (Exception e) {
            Exception cause = new IllegalArgumentException(e.getMessage());
            throw new WebApplicationException(cause, Response.Status.UNAUTHORIZED);
        }
    }

    @Provider
    public static class ServiceAuthenticationFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            if (resourceInfo.getResourceMethod().getAnnotation(ServiceAuthorization.class) != null) {
                context.register(ServiceAuthenticationFilter.class);
            }
        }
    }
}