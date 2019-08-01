package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

@ApplicationScoped
public class OrganisasjonAdapter {

    private OrganisasjonConsumer organisasjonConsumer;

    public OrganisasjonAdapter() {
        // For CDI
    }

    @Inject
    public OrganisasjonAdapter(OrganisasjonConsumer organisasjonConsumer) {
        this.organisasjonConsumer = organisasjonConsumer;
    }

    public OrganisasjonInfo hentOrganisasjonInfo(String orgnummer) {
        HentOrganisasjonRequest hentOrganisasjonRequest = new HentOrganisasjonRequest(orgnummer);
        try {
            HentOrganisasjonResponse hentOrganisasjonResponse = organisasjonConsumer.hentOrganisasjon(hentOrganisasjonRequest);
            if (null != hentOrganisasjonResponse) {
                Organisasjon organisasjon = hentOrganisasjonResponse.getOrganisasjon();
                String organisasjonNavn = ((UstrukturertNavn) organisasjon.getNavn()).getNavnelinje().stream().filter(it -> !it.isEmpty())
                        .reduce("", (a, b) -> a + " " + b).trim();
                return new OrganisasjonInfo(organisasjon.getOrgnummer(), organisasjonNavn);
            }

        } catch (HentOrganisasjonOrganisasjonIkkeFunnet hentOrganisasjonOrganisasjonIkkeFunnet) {
            throw OrganisasjonFeilmeldinger.FACTORY.fantIkkeOrganisasjon(hentOrganisasjonOrganisasjonIkkeFunnet).toException();
        } catch (HentOrganisasjonUgyldigInput hentOrganisasjonUgyldigInput) {
            throw OrganisasjonFeilmeldinger.FACTORY.ugyldigInput(hentOrganisasjonUgyldigInput).toException();
        }
        return null;
    }
}
