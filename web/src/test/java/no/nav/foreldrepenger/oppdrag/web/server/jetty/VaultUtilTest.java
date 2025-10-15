package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VaultUtilTest {

    @BeforeEach
    void setUp() {
        System.setProperty(VaultUtil.VAULT_MOUNT_PATH, "src/test/resources");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(VaultUtil.VAULT_MOUNT_PATH);
    }

    @Test
    void testFileRead() {
        var verdi = VaultUtil.lesFilVerdi("/vault/secret", "user");
        assertThat(verdi).isEqualTo("srvTest");
    }
}
