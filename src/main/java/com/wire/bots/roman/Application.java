// Wire
// Copyright (C) 2016 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

package com.wire.bots.roman;

import com.wire.bots.roman.commands.UpdateCertCommand;
import com.wire.bots.roman.filters.BackendAuthenticationFilter;
import com.wire.bots.roman.filters.ProxyAuthenticationFilter;
import com.wire.bots.roman.filters.ServiceAuthenticationFilter;
import com.wire.bots.roman.filters.ServiceTokenAuthenticationFilter;
import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.resources.*;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import com.wire.bots.sdk.factories.CryptoFactory;
import com.wire.bots.sdk.factories.StorageFactory;
import io.dropwizard.bundles.redirect.PathRedirect;
import io.dropwizard.bundles.redirect.RedirectBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class Application extends Server<Config> {
    private static Application instance;
    private Key key;

    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    public static Key getKey() {
        return instance.key;
    }

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);
        instance = (Application) bootstrap.getApplication();

        bootstrap.addBundle(new WebsocketBundle(WebSocket.class));
        bootstrap.addCommand(new UpdateCertCommand());
        bootstrap.addBundle(new RedirectBundle(new PathRedirect("/", "/swagger#/default")));
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        return new MessageHandler(jdbi, getClient());
    }

    @Override
    protected void registerFeatures() {
        environment.jersey().register(ProxyAuthenticationFilter.ProxyAuthenticationFeature.class);
        environment.jersey().register(ServiceAuthenticationFilter.ServiceAuthenticationFeature.class);
        environment.jersey().register(ServiceTokenAuthenticationFilter.ServiceTokenAuthenticationFeature.class);
        environment.jersey().register(BackendAuthenticationFilter.BackendAuthenticationFeature.class);
    }

    @Override
    protected void initialize(Config config, Environment env) {
        this.config = config;
        this.key = Keys.hmacShaKeyFor(config.key.getBytes());
    }

    @Override
    protected void onRun(Config config, Environment env) {
        ProviderClient providerClient = new ProviderClient(getClient(), config.apiHost);

        addResource(new ProviderResource(jdbi, providerClient));
        addResource(new ServiceResource(jdbi, providerClient));
        addResource(new ConversationResource(getRepo()));
        addResource(new UsersResource(getRepo()));
        addResource(new BroadcastResource(jdbi, getRepo()));
        addResource(new MessagesResource());
    }

    @Override
    protected ClientRepo createClientRepo() {
        StorageFactory storageFactory = getStorageFactory();
        CryptoFactory cryptoFactory = getCryptoFactory();
        return new CachedClientRepo(getClient(), cryptoFactory, storageFactory);
    }
}
