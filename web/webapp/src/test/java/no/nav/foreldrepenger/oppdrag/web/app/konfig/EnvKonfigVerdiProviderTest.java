package no.nav.foreldrepenger.oppdrag.web.app.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.vedtak.konfig.KonfigVerdi;

public class EnvKonfigVerdiProviderTest {

    @Test
    public void getPrioritet() {
        EnvKonfigVerdiProvider provider = new EnvKonfigVerdiProvider();
        assertThat(provider.getPrioritet()).isEqualTo(20);
    }

    @Test
    public void getVerdi() {
        KonfigVerdi.Converter<Integer> converter = new KonfigVerdi.IntegerConverter();
        EnvKonfigVerdiProvider provider = new EnvKonfigVerdiProvider();
        assertThat(provider.getVerdi("bob", converter)).isNull();
    }

    @Test
    public void harVerdi() {
        EnvKonfigVerdiProvider provider = new EnvKonfigVerdiProvider();
        assertThat(provider.harVerdi("tullOgTøys")).isFalse();
    }
}
