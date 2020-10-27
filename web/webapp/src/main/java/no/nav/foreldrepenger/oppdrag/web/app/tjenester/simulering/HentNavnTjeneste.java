package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonInfo;
import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.impl.Personinfo;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

@ApplicationScoped
class HentNavnTjeneste {

    private TpsTjeneste tpsTjeneste;

    private OrganisasjonTjeneste organisasjonTjeneste;

    HentNavnTjeneste() {

    }

    @Inject
    public HentNavnTjeneste(TpsTjeneste tpsTjeneste, OrganisasjonTjeneste organisasjonTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.organisasjonTjeneste = organisasjonTjeneste;
    }

    public AktørId hentAktørIdGittFnr(String fnr) {
        return tpsTjeneste.hentAktørForFnr(new PersonIdent(fnr)).orElseThrow(() -> HentNavnTjenesteFeil.FACTORY.kanIkkeFinneAktørId().toException());
    }

    public String hentNavnGittFnr(String fnr) {
        Optional<AktørId> aktørId = tpsTjeneste.hentAktørForFnr(new PersonIdent(fnr));
        if (!aktørId.isPresent()) {
            throw HentNavnTjenesteFeil.FACTORY.kanIkkeFinneAktørId().toException();
        }
        Optional<Personinfo> funnetPersoninfo = tpsTjeneste.hentPersoninfoForAktør(aktørId.get());
        if (!funnetPersoninfo.isPresent()) {
            throw HentNavnTjenesteFeil.FACTORY.kanIkkeFinnePersoninfo().toException();
        }
        return funnetPersoninfo.get().getNavn();
    }

    public String hentNavnGittOrgnummer(String orgnummer) {
        return organisasjonTjeneste.hentOrganisasjonInfo(orgnummer).map(OrganisasjonInfo::getNavn)
                .orElseThrow(() -> HentNavnTjenesteFeil.FACTORY.kanIkkeFinneOrganisasjoninfo().toException());
    }

    interface HentNavnTjenesteFeil extends DeklarerteFeil {
        HentNavnTjenesteFeil FACTORY = FeilFactory.create(HentNavnTjenesteFeil.class);

        @IntegrasjonFeil(feilkode = "FPO-118600", feilmelding = "Kunne ikke finne aktørid", logLevel = LogLevel.WARN)
        Feil kanIkkeFinneAktørId();

        @IntegrasjonFeil(feilkode = "FPO-046823", feilmelding = "Kunne ikke finne personinfo", logLevel = LogLevel.WARN)
        Feil kanIkkeFinnePersoninfo();

        @IntegrasjonFeil(feilkode = "FPO-069991", feilmelding = "Kunne ikke finne organisasjoninfo", logLevel = LogLevel.WARN)
        Feil kanIkkeFinneOrganisasjoninfo();
    }

}
