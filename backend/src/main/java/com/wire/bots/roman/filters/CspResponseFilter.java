package com.wire.bots.roman.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

public class CspResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // this is going to be
        // swagger.json, swagger-static, swagger-ui and all other assets
        if (requestContext.getUriInfo().getPath().contains("swagger")) {
            responseContext.getHeaders().add("Content-Security-Policy",
                    "default-src 'self'; connect-src 'self'; media-src data:; img-src 'self' data:; " +
                            "style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline';");
        }
    }

}
