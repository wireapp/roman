package com.wire.bots.roman.resources;

import com.wire.bots.cryptobox.CryptoException;
import com.wire.bots.roman.resources.dummies.AuthenticationFeatureDummy;
import com.wire.bots.roman.resources.dummies.WireClientDummy;
import com.wire.lithium.ClientRepo;
import com.wire.xenon.backend.models.User;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static com.wire.bots.roman.resources.dummies.Const.BOT_ID;
import static com.wire.bots.roman.resources.dummies.Const.USER_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
public class UsersResourceTest {
    private static final ClientRepo clientRepo = mock(ClientRepo.class);

    public static final ResourceExtension resources = ResourceExtension.builder()
            .addProvider(AuthenticationFeatureDummy.class)
            .addResource(new UsersResource(clientRepo))
            .build();

    @BeforeEach
    public void setup() throws IOException, CryptoException {
        when(clientRepo.getClient(BOT_ID)).thenReturn(new WireClientDummy());
    }

    @AfterEach
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
