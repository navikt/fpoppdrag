package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonInfo;
import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

@ApplicationScoped
class HentNavnTjeneste {

    private PersonTjeneste tpsTjeneste;

    private OrganisasjonTjeneste organisasjonTjeneste;

    HentNavnTjeneste() {

    }

    @Inject
    public HentNavnTjeneste(PersonTjeneste tpsTjeneste, OrganisasjonTjeneste organisasjonTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.organisasjonTjeneste = organisasjonTjeneste;
    }

    public AktørId hentAktørIdGittFnr(String fnr) {
        return tpsTjeneste.hentAktørForFnr(new PersonIdent(fnr)).orElseThrow(() -> HentNavnTjenesteFeil.FACTORY.kanIkkeFinneAktørId().toException());
    }

    public String hentNavnGittFnr(String fnr) {
        return tpsTjeneste.hentNavnFor(new PersonIdent(fnr)).orElse("Ukjent navn");
    }

    public String hentNavnGittOrgnummer(String orgnummer) {
        return organisasjonTjeneste.hentOrganisasjonInfo(orgnummer).map(OrganisasjonInfo::getNavn)
                .orElseThrow(() -> HentNavnTjenesteFeil.FACTORY.kanIkkeFinneOrganisasjoninfo().toException());
    }

    interface HentNavnTjenesteFeil extends DeklarerteFeil {
        HentNavnTjenesteFeil FACTORY = FeilFactory.create(HentNavnTjenesteFeil.class);

        @IntegrasjonFeil(feilkode = "FPO-118600", feilmelding = "Kunne ikke finne aktørid", logLevel = LogLevel.WARN)
        Feil kanIkkeFinneAktørId();

        @IntegrasjonFeil(feilkode = "FPO-069991", feilmelding = "Kunne ikke finne organisasjoninfo", logLevel = LogLevel.WARN)
        Feil kanIkkeFinneOrganisasjoninfo();
    }

}
