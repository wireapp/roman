package com.wire.bots.roman;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.sdk.API;
import com.wire.bots.sdk.BotClient;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.WireClient;
import com.wire.bots.sdk.crypto.Crypto;
import com.wire.bots.sdk.factories.CryptoFactory;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.models.otr.Missing;
import com.wire.bots.sdk.models.otr.Recipients;
import com.wire.bots.sdk.server.model.NewBot;

import javax.ws.rs.client.Client;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CachedClientRepo extends ClientRepo {

    private ConcurrentHashMap<UUID, _BotClient> clients = new ConcurrentHashMap<>();

    CachedClientRepo(Client httpClient, CryptoFactory cf, StorageFactory sf) {
        super(httpClient, cf, sf);
    }

    @Override
    public WireClient getClient(UUID botId) {

        return clients.computeIfAbsent(botId, x -> {
                    try {
                        NewBot state = sf.create(botId).getState();
                        Crypto crypto = cf.create(botId);
                        API api = new API(httpClient, state.token);
                        return new _BotClient(state, crypto, api);
                    } catch (Exception e) {
                        return null;
                    }
                }
        );
    }

    private static class _BotClient extends BotClient {

        _BotClient(NewBot state, Crypto crypto, API api) {
            super(state, crypto, api);
        }

        @Override
        public void close() {
            // we dont want to close this object like ever!
        }

        @Override
        public Recipients encrypt(byte[] content, Missing devices) throws CryptoException {
            synchronized (crypto) {
                return crypto.encrypt(devices, content);
            }
        }

        @Override
        public String decrypt(UUID userId, String clientId, String cypher) throws CryptoException {
            synchronized (crypto) {
                return crypto.decrypt(userId, clientId, cypher);
            }
        }
    }
}
