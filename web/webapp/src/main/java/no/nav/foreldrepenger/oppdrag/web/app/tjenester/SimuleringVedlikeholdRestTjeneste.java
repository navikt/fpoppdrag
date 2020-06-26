package no.nav.foreldrepenger.oppdrag.web.app.tjenester;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@ApplicationScoped
@Transactional
public class SimuleringVedlikeholdRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(SimuleringVedlikeholdRestTjeneste.class);

    private EntityManager entityManager;

    public SimuleringVedlikeholdRestTjeneste() {
        //for CDI proxy
    }

    @Inject
    public SimuleringVedlikeholdRestTjeneste(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GET
    @Path("/fjern-gamle-simulering-xml")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Sletter gamle simulering-xml-er", tags = "FORVALTNING")
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT, sporingslogg = false)
    public Response slettGamleSimuleringXml(@Valid @NotNull @Min(0) @Max(10000) @QueryParam("antall") Integer antall) {
        long antallNyesteDagerSomIkkeSkalSlettes = 90;

        Query query = entityManager.createNativeQuery("delete from simulering_xml" +
                "  where opprettet_tid <=" +
                "   (select max(opprettet_tid) from (" +
                "       select opprettet_tid" +
                "       from simulering_xml" +
                "       where opprettet_tid < systimestamp - :uslettbareDager" +
                "       order by opprettet_tid" +
                "    )" +
                "    where rownum <= :antall" +
                "  )")
                .setParameter("uslettbareDager", antallNyesteDagerSomIkkeSkalSlettes)
                .setParameter("antall", antall);
        int resultat = query.executeUpdate();

        logger.info("Slettet inntil {} gamle simulering-xml-er. {}", antall, resultat);
        return Response.ok().build();
    }
}
