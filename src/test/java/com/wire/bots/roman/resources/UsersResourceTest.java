package com.wire.bots.roman.resources;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.resources.dummies.AuthenticationFeatureDummy;
import com.wire.bots.roman.resources.dummies.WireClientDummy;
import com.wire.bots.sdk.ClientRepo;
import com.wire.bots.sdk.server.model.User;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static com.wire.bots.roman.resources.dummies.Const.BOT_ID;
import static com.wire.bots.roman.resources.dummies.Const.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UsersResourceTest {
    private static final ClientRepo clientRepo = mock(ClientRepo.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(AuthenticationFeatureDummy.class)
            .addResource(new UsersResource(clientRepo))
            .build();

    @Before
    public void setup() throws IOException, CryptoException {
        when(clientRepo.getClient(BOT_ID)).thenReturn(new WireClientDummy());
    }

    @After
    public void tearDown() {
        reset(clientRepo);
    }

    @Test
    public void testGetUser() {
        final User actual = resources
                .target("users")
                .path(USER_ID.toString())
                .request()
                .get(User.class);

        assertThat(actual.id).isEqualTo(USER_ID);
    }
}
