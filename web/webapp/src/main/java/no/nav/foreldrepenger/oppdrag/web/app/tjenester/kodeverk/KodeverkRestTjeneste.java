package no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.APPLIKASJON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.oppdrag.kodeverk.Kodeliste;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = {"kodeverk"})
@Path("/kodeverk")
@RequestScoped
@Transaction
public class KodeverkRestTjeneste {

    private HentKodeverkTjeneste hentKodeverkTjeneste; // NOSONAR

    @Inject
    public KodeverkRestTjeneste(HentKodeverkTjeneste hentKodeverkTjeneste) {
        this.hentKodeverkTjeneste = hentKodeverkTjeneste;
    }

    public KodeverkRestTjeneste() {
        // for resteasy
    }

    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henter kodeliste", notes = ("Returnerer gruppert kodeliste."))
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON)
    public Map<String, Object> hentGruppertKodeliste() {
        Map<String, Object> kodelisterGruppertPåType = new HashMap<>();

        Map<String, List<Kodeliste>> grupperteKodelister = hentKodeverkTjeneste.hentGruppertKodeliste();
        grupperteKodelister.forEach(kodelisterGruppertPåType::put);

        return kodelisterGruppertPåType;
    }
}
