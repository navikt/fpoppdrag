
package no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.kodeverk.Kodeliste;
import no.nav.foreldrepenger.oppdrag.kodeverk.KodeverkRepositoryImpl;

public class HentKodeverkTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Test
    public void hentGruppertKodeliste() {
        HentKodeverkTjeneste hentKodeverkTjeneste = new HentKodeverkTjenesteImpl(new KodeverkRepositoryImpl(repoRule.getEntityManager()));
        Map<String, List<Kodeliste>> gruppertKodeliste = hentKodeverkTjeneste.hentGruppertKodeliste();
        assertThat(gruppertKodeliste.keySet()).isNotEmpty();
    }
}