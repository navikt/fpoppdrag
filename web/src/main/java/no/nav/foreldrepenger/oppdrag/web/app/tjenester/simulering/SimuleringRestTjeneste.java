package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static no.nav.foreldrepenger.oppdrag.web.app.abac.FPOppdragBeskyttetRessursAttributt.FAGSAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.UPDATE;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.StartSimuleringTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.BehandlingIdDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimulerOppdragDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;


@RequestScoped
@Path("simulering")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
@SecurityRequirements(@SecurityRequirement(name = "openIdConnect", scopes = "openid"))
public class SimuleringRestTjeneste {

    private SimuleringResultatTjeneste simuleringResultatTjeneste;
    private StartSimuleringTjeneste startSimuleringTjeneste;

    public SimuleringRestTjeneste() {
        // For CDI
    }

    @Inject
    public SimuleringRestTjeneste(SimuleringResultatTjeneste simuleringResultatTjeneste, StartSimuleringTjeneste startSimuleringTjeneste) {
        this.simuleringResultatTjeneste = simuleringResultatTjeneste;
        this.startSimuleringTjeneste = startSimuleringTjeneste;
    }

    @POST
    @Path("resultat")
    @Operation(description = "Hent resultat av simulering mot økonomi", summary = ("Returnerer simuleringsresultat."), tags = "simulering")
    @BeskyttetRessurs(action = READ, resource = FAGSAK)
    public SimuleringResultatDto hentSimuleringResultat(@Valid BehandlingIdDto behandlingIdDto) {
        Optional<SimuleringResultatDto> optionalSimuleringResultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingIdDto.getBehandlingId());
        return optionalSimuleringResultatDto.orElse(null);
    }

    @POST
    @Path("resultat-uten-inntrekk")
    @Operation(description = "Hent detaljert resultat av simulering mot økonomi med og uten inntrekk", summary = ("Returnerer simuleringsresultat."), tags = "simulering")
    @BeskyttetRessurs(action = READ, resource = FAGSAK)
    public SimuleringDto hentSimuleringResultatMedOgUtenInntrekk(@Valid BehandlingIdDto behandlingIdDto) {
        Optional<SimuleringDto> optionalSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingIdDto.getBehandlingId());
        return optionalSimuleringDto.orElse(null);
    }

    @POST
    @Path("start")
    @Operation(description = "Start simulering for behandling med oppdrag", summary = ("Returnerer status på om oppdrag er gyldig"), tags = "simulering")
    @BeskyttetRessurs(action = UPDATE, resource = FAGSAK)
    public Response startSimulering(@Valid SimulerOppdragDto simulerOppdragDto) {
        final Long behandlingId = simulerOppdragDto.getBehandlingId();
        startSimuleringTjeneste.startSimulering(behandlingId, simulerOppdragDto.getOppdragPrMottakerDecoded());

        return Response.ok().build();
    }

    @POST
    @Path("kanseller")
    @Operation(description = "Kanseller simulering for behandling", summary = ("Deaktiverer simuleringgrunnlag for behandling"), tags = "simulering")
    @BeskyttetRessurs(action = UPDATE, resource = FAGSAK)
    public Response kansellerSimulering(@Valid BehandlingIdDto behandlingIdDto) {
        startSimuleringTjeneste.kansellerSimulering(behandlingIdDto.getBehandlingId());
        return Response.ok().build();
    }

    @POST
    @Path("feilutbetalte-perioder")
    @Operation(description = "Hent sum feilutbetaling og simulerte perioder som er feilutbetalte og kan kreves tilbake fra brukeren.", summary = ("Returnerer perioder som er feilutbetalt."), tags = "simulering")
    @BeskyttetRessurs(action = READ, resource = FAGSAK)
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioderForTilbakekreving(@Valid BehandlingIdDto behandlingIdDto) {
        return simuleringResultatTjeneste.hentFeilutbetaltePerioder(behandlingIdDto.getBehandlingId());
    }
}
