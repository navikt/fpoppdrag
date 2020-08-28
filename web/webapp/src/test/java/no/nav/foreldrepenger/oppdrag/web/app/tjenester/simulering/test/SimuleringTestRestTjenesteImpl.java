package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.util.env.Environment;

/**
 * Dette class er for test tjeneste og må fjernes før lansering
 */
@RequestScoped
@Path("simulering")
@Transactional
public class SimuleringTestRestTjenesteImpl implements SimuleringTestRestTjeneste{

    private SimuleringTestTjeneste simuleringTestTjeneste;

    public SimuleringTestRestTjenesteImpl() {
        // For cdi proxy
    }

    @Inject
    public SimuleringTestRestTjenesteImpl(SimuleringTestTjeneste simuleringTestTjeneste) {
        this.simuleringTestTjeneste = simuleringTestTjeneste;
    }

    @POST
    @Path("/testdata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagre simulering test data", summary = ("Brukes til intern testing"), tags = "simtest")
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    public Response lagreSimuleringTestData(@NotNull @Valid SimuleringGjelderDto simuleringGjelderDto) {
        if (Environment.current().isProd()) {
            throw new IllegalStateException(getClass().getName() + " skal ikke være tilgjengelig i produksjonsmijøet.");
        }
        simuleringTestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);
        return Response.status(HttpStatus.SC_CREATED).build();
    }

}
