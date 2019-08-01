package no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.app;

import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.oppdrag.kodeverk.Kodeliste;

public interface HentKodeverkTjeneste {

    Map<String, List<Kodeliste>> hentGruppertKodeliste();
}
