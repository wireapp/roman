package com.wire.bots.roman.resources.dummies;

import com.wire.bots.roman.filters.ProxyAuthorization;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationFeatureDummy implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(ProxyAuthorization.class) != null) {
            context.register(AuthenticationFilterDummy.class);
        }
    }
}
