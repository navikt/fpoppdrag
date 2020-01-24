package no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.APPLIKASJON;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Kodeverdi;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/kodeverk")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class KodeverkRestTjeneste {

    public static final Map<String, Collection<? extends Kodeverdi>> KODEVERDIER_SOM_BRUKES_PÅ_KLIENT;
    static {
        Map<String, Collection<? extends Kodeverdi>> map = new LinkedHashMap<>();
        map.put(Fagsystem.class.getSimpleName(), Fagsystem.kodeMap().values());
        Map<String, Collection<? extends Kodeverdi>> mapFiltered = new LinkedHashMap<>();

        map.forEach((key, value) -> mapFiltered.put(key, value.stream().filter(f -> !"-".equals(f.getKode())).collect(Collectors.toSet())));

        KODEVERDIER_SOM_BRUKES_PÅ_KLIENT = Collections.unmodifiableMap(mapFiltered);
    }

    @Inject
    public KodeverkRestTjeneste() {
    }

    @GET
    @Operation(description = "Henter kodeliste", tags = "kodeverk")
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Map<String, Object> hentGruppertKodeliste() {
        Map<String, Object> kodelisterGruppertPåType = new HashMap<>();

        KODEVERDIER_SOM_BRUKES_PÅ_KLIENT.forEach(kodelisterGruppertPåType::put);

        return kodelisterGruppertPåType;
    }
}
