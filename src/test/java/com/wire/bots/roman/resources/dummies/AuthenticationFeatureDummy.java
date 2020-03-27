package com.wire.bots.roman.resources.dummies;

import com.wire.bots.roman.filters.ProxyAuthorization;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationFeatureDummy implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().getAnnotation(ProxyAuthorization.class) != null) {
            context.register(AuthenticationFilterDummy.class);
        }
    }
}
