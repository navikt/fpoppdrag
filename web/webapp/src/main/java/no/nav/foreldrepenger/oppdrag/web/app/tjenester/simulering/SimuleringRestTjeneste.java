package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.StartSimuleringTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.BehandlingIdDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimulerOppdragDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;


@Api(tags = {"simulering"})
@RequestScoped
@Path("simulering")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transaction
public class SimuleringRestTjeneste {

    private SimuleringResultatTjeneste simuleringResultatTjeneste;
    private StartSimuleringTjeneste startSimuleringTjeneste;

    public SimuleringRestTjeneste() {
        // For resteasy
    }

    @Inject
    public SimuleringRestTjeneste(SimuleringResultatTjeneste simuleringResultatTjeneste, StartSimuleringTjeneste startSimuleringTjeneste) {
        this.simuleringResultatTjeneste = simuleringResultatTjeneste;
        this.startSimuleringTjeneste = startSimuleringTjeneste;
    }


    @POST
    @Path("resultat")
    @Timed
    @ApiOperation(value = "Hent resultat av simulering mot økonomi", notes = ("Returnerer simuleringsresultat."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public SimuleringResultatDto hentSimuleringResultat(@Valid BehandlingIdDto behandlingIdDto) {
        if (1 == 1) {
            return new SimuleringResultatDto.Builder()
                    .medSumFeilutbetaling(BigDecimal.valueOf(1000))
                    .medSumInntrekk(BigDecimal.ZERO)
                    .medSlåttAvInntrekk(false)
                    .build();
        }
        Optional<SimuleringResultatDto> optionalSimuleringResultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingIdDto.getBehandlingId());
        return optionalSimuleringResultatDto.orElse(null);
    }

    @POST
    @Path("resultat-uten-inntrekk")
    @Timed
    @ApiOperation(value = "Hent detaljert resultat av simulering mot økonomi med og uten inntrekk", notes = ("Returnerer simuleringsresultat."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public SimuleringDto hentSimuleringResultatMedOgUtenInntrekk(@Valid BehandlingIdDto behandlingIdDto) {
        Optional<SimuleringDto> optionalSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingIdDto.getBehandlingId());
        return optionalSimuleringDto.orElse(null);
    }

    @POST
    @Path("start")
    @Timed
    @ApiOperation(value = "Start simulering for behandling med oppdrag", notes = "Returnerer status på om oppdrag er gyldig")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response startSimulering(@Valid SimulerOppdragDto simulerOppdragDto) {
        final Long behandlingId = simulerOppdragDto.getBehandlingId();
        startSimuleringTjeneste.startSimulering(behandlingId, simulerOppdragDto.getOppdragPrMottakerDecoded());
        return Response.ok().build();
    }

    @POST
    @Path("kanseller")
    @Timed
    @ApiOperation(value = "Kanseller simulering for behandling", notes = "Deaktiverer simuleringgrunnlag for behandling")
    @BeskyttetRessurs(action = UPDATE, ressurs = FAGSAK)
    public Response kansellerSimulering(@Valid BehandlingIdDto behandlingIdDto) {
        startSimuleringTjeneste.kansellerSimulering(behandlingIdDto.getBehandlingId());
        return Response.ok().build();
    }

    @POST
    @Path("feilutbetalte-perioder")
    @Timed
    @ApiOperation(value = "Hent sum feilutbetaling og simulerte perioder som er feilutbetalte og kan kreves tilbake fra brukeren.", notes = ("Returnerer perioder som er feilutbetalt."))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioderForTilbakekreving(@Valid BehandlingIdDto behandlingIdDto) {
        if (1 == 1) {
            return new FeilutbetaltePerioderDto(1000L, new ArrayList<>());
        }
        return simuleringResultatTjeneste.hentFeilutbetaltePerioder(behandlingIdDto.getBehandlingId());
    }
}
