package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrgInfo;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class OrganisasjonTjeneste {
    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);

    private LRUCache<String, OrganisasjonInfo> cache = new LRUCache<>(500, CACHE_ELEMENT_LIVE_TIME_MS);

    private OrgInfo organisasjonAdapter;

    public OrganisasjonTjeneste() {
        // For CDI
    }

    @Inject
    public OrganisasjonTjeneste(OrgInfo organisasjonAdapter) {
        this.organisasjonAdapter = organisasjonAdapter;
    }

    public Optional<OrganisasjonInfo> hentOrganisasjonInfo(String orgnummer) {
        return Optional.ofNullable(hent(orgnummer));
    }

    private OrganisasjonInfo hent(String orgnr) {
        var response = Optional.ofNullable(cache.get(orgnr)).orElseGet(() -> hentOrganisasjonRest(orgnr));
        cache.put(orgnr, response);
        return response;
    }

    private OrganisasjonInfo hentOrganisasjonRest(String orgNummer) {
        Objects.requireNonNull(orgNummer, "orgNummer"); // NOSONAR
        var org = organisasjonAdapter.hentOrganisasjonNavn(orgNummer);
        return new OrganisasjonInfo(orgNummer, org);
    }
}
