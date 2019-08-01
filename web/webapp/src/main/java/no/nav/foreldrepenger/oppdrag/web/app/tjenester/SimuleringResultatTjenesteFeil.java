package no.nav.foreldrepenger.oppdrag.web.app.tjenester;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface SimuleringResultatTjenesteFeil extends DeklarerteFeil {
    SimuleringResultatTjenesteFeil FACTORY = FeilFactory.create(SimuleringResultatTjenesteFeil.class);

    @TekniskFeil(feilkode = "FPO-319832", feilmelding = "Fant ikke simuleringsresultat for behandlingId=%s", logLevel = LogLevel.WARN)
    Feil finnesIkkeSimuleringsResultat(Long behandlingId);

    @TekniskFeil(feilkode = "FPO-216725", feilmelding = "Fant ingen perioder med feilutbetaling for bruker, behandlingId=%s", logLevel = LogLevel.WARN)
    Feil finnesIkkeFeilutbetalingsperioderForBruker(Long behandlingId);
}
