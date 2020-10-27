package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

@ApplicationScoped
class HentNavnTjeneste {

    private TpsTjeneste tpsTjeneste;

    HentNavnTjeneste() {

    }

    @Inject
    public HentNavnTjeneste(TpsTjeneste tpsTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
    }

    public AktørId hentAktørIdGittFnr(String fnr) {
        return tpsTjeneste.hentAktørIdForPersonIdent(new PersonIdent(fnr)).orElseThrow(() -> HentNavnTjenesteFeil.FACTORY.kanIkkeFinneAktørId().toException());
    }

    interface HentNavnTjenesteFeil extends DeklarerteFeil {
        HentNavnTjenesteFeil FACTORY = FeilFactory.create(HentNavnTjenesteFeil.class);

        @IntegrasjonFeil(feilkode = "FPO-118600", feilmelding = "Kunne ikke finne aktørid", logLevel = LogLevel.WARN)
        Feil kanIkkeFinneAktørId();
    }

}
