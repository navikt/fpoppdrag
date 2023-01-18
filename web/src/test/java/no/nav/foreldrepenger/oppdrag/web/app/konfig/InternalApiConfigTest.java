package no.nav.foreldrepenger.oppdrag.web.app.konfig;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;


public class InternalApiConfigTest {

    @Test
    public void getClasses() {
        InternalApiConfig app = new InternalApiConfig();
        Set<Class<?>> classes = app.getClasses();
        assertThat(classes).hasSize(2);
    }
}
