package no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.oppdrag.kodeverk.Fagsystem;
import no.nav.foreldrepenger.oppdrag.kodeverk.Kodeliste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;

public class KodeverkRestTjenesteTest {

    @Test
    public void skal_hente_kodeverk_og_gruppere_på_kodeverknavn() {
        HentKodeverkTjeneste hentKodeverkTjeneste = Mockito.mock(HentKodeverkTjeneste.class);
        Mockito.when(hentKodeverkTjeneste.hentGruppertKodeliste()).thenReturn(getGruppertKodeliste());

        KodeverkRestTjeneste tjeneste = new KodeverkRestTjeneste(hentKodeverkTjeneste);
        Map<String, Object> gruppertKodeliste = tjeneste.hentGruppertKodeliste();

        assertThat(gruppertKodeliste.keySet()).containsOnly(Fagsystem.class.getSimpleName());
        assertThat(gruppertKodeliste.get(Fagsystem.class.getSimpleName()))
                .isEqualTo(Arrays.asList(Fagsystem.ARENA, Fagsystem.FPSAK));
    }

    private static Map<String, List<Kodeliste>> getGruppertKodeliste() {
        Map<String, List<Kodeliste>> map = new HashMap<>();
        map.put(Fagsystem.class.getSimpleName(), Arrays.asList(Fagsystem.ARENA, Fagsystem.FPSAK));
        return map;
    }
}
