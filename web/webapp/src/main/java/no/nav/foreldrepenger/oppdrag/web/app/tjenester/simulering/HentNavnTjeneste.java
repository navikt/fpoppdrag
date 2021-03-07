package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonInfo;
import no.nav.foreldrepenger.oppdrag.domene.organisasjon.OrganisasjonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonIdent;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.vedtak.exception.IntegrasjonException;

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
        return tpsTjeneste.hentAktørForFnr(new PersonIdent(fnr)).orElseThrow(() -> new IntegrasjonException("FPO-118600", "Kunne ikke finne aktørid"));
    }

    public String hentNavnGittFnr(String fnr) {
        return tpsTjeneste.hentNavnFor(new PersonIdent(fnr)).orElse("Ukjent navn");
    }

    public String hentNavnGittOrgnummer(String orgnummer) {
        return organisasjonTjeneste.hentOrganisasjonInfo(orgnummer).map(OrganisasjonInfo::getNavn)
                .orElseThrow(() -> new IntegrasjonException("FPO-069991", "Kunne ikke finne organisasjoninfo"));
    }

}
