package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class OrganisasjonTjeneste {

    private OrganisasjonAdapter organisasjonAdapter;

    public OrganisasjonTjeneste() {
        // For CDI
    }

    @Inject
    public OrganisasjonTjeneste(OrganisasjonAdapter organisasjonAdapter) {
        this.organisasjonAdapter = organisasjonAdapter;
    }

    public Optional<OrganisasjonInfo> hentOrganisasjonInfo(String orgnummer) {
        return Optional.ofNullable(organisasjonAdapter.hentOrganisasjonInfo(orgnummer));
    }
}
