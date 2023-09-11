package no.nav.foreldrepenger.oppdrag.domenetjenester.person;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PersonTjeneste {

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private LRUCache<String, AktørId> cacheIdentTilAktørId;

    private Persondata pdlKlient;

    public PersonTjeneste() {
        // for CDI proxy
    }

    @Inject
    public PersonTjeneste(Persondata pdlKlient) {
        this.pdlKlient = pdlKlient;
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public Optional<AktørId> hentAktørForFnr(String fnr) {
        var fraCache = cacheIdentTilAktørId.get(fnr);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        var request = new HentIdenterQueryRequest();
        request.setIdent(fnr);
        request.setGrupper(List.of(IdentGruppe.AKTORID));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection().ident());

        final Identliste identliste;

        try {
            identliste = pdlKlient.hentIdenter(request, projection);
        } catch (VLException v) {
            if (Persondata.PDL_KLIENT_NOT_FOUND_KODE.equals(v.getKode())) {
                return Optional.empty();
            }
            throw v;
        }

        var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).map(AktørId::new);
        aktørId.ifPresent(a -> cacheIdentTilAktørId.put(fnr, a)); // Kan ikke legge til i cache aktørId -> ident ettersom ident kan være ikke-current
        return aktørId;
    }

}
