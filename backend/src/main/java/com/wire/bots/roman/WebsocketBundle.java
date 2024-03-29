/**
 * The MIT License
 * Copyright (c) 2017 LivePerson, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.wire.bots.roman;

import com.wire.bots.roman.model.Config;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

public class WebsocketBundle implements ConfiguredBundle<Config>, LifeCycle.Listener {

    private final Collection<ServerEndpointConfig> endpointConfigs = new ArrayList<>();
    private static final Logger LOG = LoggerFactory.getLogger(WebsocketBundle.class);
    volatile boolean starting = false;
    private final ServerEndpointConfig.Configurator defaultConfigurator;


    public WebsocketBundle(ServerEndpointConfig.Configurator defaultConfigurator, Class<?>... endpoints) {
        this(defaultConfigurator, Arrays.asList(endpoints), new ArrayList<>());
    }

    public WebsocketBundle(Class<?>... endpoints) {
        this(null, Arrays.asList(endpoints), new ArrayList<>());
    }

    public WebsocketBundle(ServerEndpointConfig... configs) {
        this(null, new ArrayList<>(), Arrays.asList(configs));
    }

    public WebsocketBundle(ServerEndpointConfig.Configurator defaultConfigurator, Collection<Class<?>> endpointClasses, Collection<ServerEndpointConfig> serverEndpointConfigs) {
        this.defaultConfigurator = defaultConfigurator;
        endpointClasses.forEach(this::addEndpoint);
        this.endpointConfigs.addAll(serverEndpointConfigs);
    }

    public void addEndpoint(ServerEndpointConfig epC) {
        endpointConfigs.add(epC);
        if (starting)
            throw new RuntimeException("can't add endpoint after starting lifecycle");
    }

    public void addEndpoint(Class<?> clazz) {
        ServerEndpoint anno = clazz.getAnnotation(ServerEndpoint.class);
        if (anno == null) {
            throw new RuntimeException(clazz.getCanonicalName() + " does not have a " + ServerEndpoint.class.getCanonicalName() + " annotation");
        }
        ServerEndpointConfig.Builder bldr = ServerEndpointConfig.Builder.create(clazz, anno.value());
        if (defaultConfigurator != null) {
            bldr.configurator(defaultConfigurator);
        }
        endpointConfigs.add(bldr.build());
        if (starting)
            throw new RuntimeException("can't add endpoint after starting lifecycle");
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Config config, Environment environment) {
        environment.lifecycle().addEventListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                starting = true;
                JakartaWebSocketServletContainerInitializer.configure(environment.getApplicationContext(), (servletContext, wsContainer) ->
                {
                    // This lambda will be called at the appropriate place in the
                    // ServletContext initialization phase where you can initialize
                    // and configure your websocket container.
                    // Configure defaults for container
                    wsContainer.setDefaultMaxTextMessageBufferSize(65535);

                    // Add WebSocket endpoint to jakarta.websocket layer
                    StringBuilder sb = new StringBuilder("Registering websocket endpoints: ")
                            .append(System.lineSeparator())
                            .append(System.lineSeparator());
                    endpointConfigs.forEach(rethrow(conf -> addEndpoint(wsContainer, conf, sb)));
                    LOG.info(sb.toString());
                });
            }

            private void addEndpoint(ServerContainer wsContainer, ServerEndpointConfig conf, StringBuilder sb) throws DeploymentException {
                wsContainer.addEndpoint(conf);
                sb.append(String.format("    WS      %s (%s)", conf.getPath(), conf.getEndpointClass().getName())).append(System.lineSeparator());
            }
        });
    }

    public static <T> Consumer<T> rethrow(ConsumerCheckException<T> c) {
        return t -> {
            try {
                c.accept(t);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    @FunctionalInterface
    public interface ConsumerCheckException<T> {
        void accept(T elem) throws Exception;
    }
}