package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

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
import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

/**
 * Dette class er for test tjeneste og må fjernes før lansering
 */
@RequestScoped
@Path("simulering")
@Transactional
public class SimuleringTestRestTjeneste {

    private SimuleringTestTjeneste simuleringTestTjeneste;
    private Unleash unleash;

    public SimuleringTestRestTjeneste() {
        // For resteasy
    }

    @Inject
    public SimuleringTestRestTjeneste(SimuleringTestTjeneste simuleringTestTjeneste, Unleash unleash) {
        this.simuleringTestTjeneste = simuleringTestTjeneste;
        this.unleash = unleash;
    }

    @POST
    @Path("/testdata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Lagre simulering test data", summary = ("Brukes til intern testing"), tags = "simtest")
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    public Response lagreSimuleringTestData(@NotNull @Valid SimuleringGjelderDto simuleringGjelderDto) {
        if (true) {
            if (erProduksjonsmiljøet()) {
                throw new IllegalStateException("fpoppdrag.testgrenesesnitt kan ikke aktiveres i produksjonsmijøet");
            }
            simuleringTestTjeneste.lagreSimuleringTestData(simuleringGjelderDto);
            return Response.status(HttpStatus.SC_CREATED).build();
        } else {
            return Response.status(HttpStatus.SC_NOT_FOUND).build();
        }
    }

    private boolean erProduksjonsmiljøet() {
        Optional<String> environmentName = EnvironmentProperty.getEnvironmentName();
        return environmentName.isPresent() && environmentName.get().toLowerCase().startsWith("p");
    }
}
