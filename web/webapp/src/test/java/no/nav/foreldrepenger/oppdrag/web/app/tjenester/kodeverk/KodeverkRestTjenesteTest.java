package no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Kodeverdi;

public class KodeverkRestTjenesteTest {

    @Test
    public void skal_hente_kodeverk_og_gruppere_på_kodeverknavn() {

        KodeverkRestTjeneste tjeneste = new KodeverkRestTjeneste();
        Map<String, Object> gruppertKodeliste = tjeneste.hentGruppertKodeliste();

        assertThat(gruppertKodeliste.keySet()).containsOnly(Fagsystem.class.getSimpleName());
        assertThat(((Set<Kodeverdi>) gruppertKodeliste.get(Fagsystem.class.getSimpleName())).stream().map(Kodeverdi::getKode).anyMatch(Fagsystem.FPSAK.getKode()::equals)).isTrue();
    }
}
