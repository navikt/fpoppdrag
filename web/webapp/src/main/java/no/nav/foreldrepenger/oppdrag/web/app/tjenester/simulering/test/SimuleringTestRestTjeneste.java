package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto.SimuleringGjelderDto;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

/**
 * Dette class er for test tjeneste og må fjernes før lansering
 */
@Api(tags = {"simulering"})
@RequestScoped
@Path("simulering")
@Transaction
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
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lagre simulering test data", notes = ("Brukes til intern testing"))
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    public Response lagreSimuleringTestData(@NotNull @Valid SimuleringGjelderDto simuleringGjelderDto) {
        if (unleash.isEnabled("fpoppdrag.testgrensesnitt")) {
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
