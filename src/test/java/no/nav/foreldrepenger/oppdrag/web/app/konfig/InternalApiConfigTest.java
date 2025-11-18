package no.nav.foreldrepenger.oppdrag.web.app.konfig;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


class InternalApiConfigTest {

    @Test
    void getClasses() {
        var app = new InternalApiConfig();
        var classes = app.getClasses();
        assertThat(classes).hasSize(2);
    }
}
