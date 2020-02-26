package com.wire.bots.roman.filters;

import com.wire.bots.roman.Application;
import io.jsonwebtoken.Jwts;

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
import java.util.UUID;

@Provider
public class ProxyAuthenticationFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (auth == null) {
            Exception cause = new IllegalArgumentException("Missing Authorization");
            throw new WebApplicationException(cause, Response.Status.UNAUTHORIZED);
        }

        String[] split = auth.split(" ");

        if (split.length != 2) {
            Exception cause = new IllegalArgumentException("Bad Authorization: missing token and/or token type");
            throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
        }

        String type = split[0];
        String token = split[1];

        if (!Objects.equals(type, "Bearer")) {
            Exception cause = new IllegalArgumentException("Bad Authorization: wrong token type");
            throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
        }

        try {
            String subject = Jwts.parser()
                    .setSigningKey(Application.getKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            UUID botId = UUID.fromString(subject);
            requestContext.setProperty("botid", botId);
        } catch (Exception e) {
            Exception cause = new IllegalArgumentException(e.getMessage());
            throw new WebApplicationException(cause, Response.Status.UNAUTHORIZED);
        }
    }

    @Provider
    public static class ProxyAuthenticationFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            if (resourceInfo.getResourceMethod().getAnnotation(ProxyAuthorization.class) != null) {
                context.register(ProxyAuthenticationFilter.class);
            }
        }
    }
}