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

import com.wire.bots.roman.model.Config;
import com.wire.bots.roman.overrides.BotResource;
import com.wire.bots.roman.overrides.InboundResource;
import com.wire.bots.roman.resources.BroadcastResource;
import com.wire.bots.roman.resources.ConversationResource;
import com.wire.bots.roman.resources.ProviderResource;
import com.wire.bots.roman.resources.ServiceResource;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.MessageHandlerBase;
import com.wire.bots.sdk.Server;
import com.wire.bots.sdk.factories.CryptoFactory;
import com.wire.bots.sdk.factories.StorageFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import io.jsonwebtoken.security.Keys;
import org.skife.jdbi.v2.DBI;

import java.security.Key;

public class Application extends Server<Config> {
    private static Application instance;

    private Key key;
    private DBI jdbi;

    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    public static Key getKey() {
        return instance.key;
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);
        instance = (Application) bootstrap.getApplication();

        WebsocketBundle bundle = new WebsocketBundle(WebSocket.class);
        bootstrap.addBundle(bundle);
    }

    @Override
    protected MessageHandlerBase createHandler(Config config, Environment env) {
        return new MessageHandler(jdbi, getClient());
    }

    @Override
    protected void initialize(Config config, Environment env) {
        this.key = Keys.hmacShaKeyFor(config.key.getBytes());
        this.jdbi = new DBIFactory().build(environment, config.database, "roman");
    }

    @Override
    protected void onRun(Config config, Environment env) {
        ProviderClient providerClient = new ProviderClient(getClient());

        addResource(new ProviderResource(jdbi, providerClient), env);
        addResource(new ServiceResource(jdbi, providerClient), env);
        addResource(new ConversationResource(getRepo()), env);
        addResource(new BroadcastResource(jdbi, getRepo()), env);
    }

    @Override
    protected void messageResource(Config config, Environment env, MessageHandlerBase handler, ClientRepo repo) {
        addResource(new InboundResource(handler, repo), env);
    }

    @Override
    protected void botResource(Config config, Environment env, MessageHandlerBase handler) {
        StorageFactory storageFactory = getStorageFactory();
        CryptoFactory cryptoFactory = getCryptoFactory();

        addResource(new BotResource(handler, storageFactory, cryptoFactory, jdbi), env);
    }
}
