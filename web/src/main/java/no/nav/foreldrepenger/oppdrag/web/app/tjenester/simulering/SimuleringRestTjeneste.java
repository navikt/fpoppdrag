package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.kontrakter.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.StartSimuleringTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.BehandlingIdDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.OppdragskontrollDtoAbacSupplier;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;


@RequestScoped
@Path("simulering")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class SimuleringRestTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(SimuleringRestTjeneste.class);

    private SimuleringResultatTjeneste simuleringResultatTjeneste;
    private StartSimuleringTjeneste startSimuleringTjenesteFpWsProxy;

    public SimuleringRestTjeneste() {
        // For CDI
    }

    @Inject
    public SimuleringRestTjeneste(SimuleringResultatTjeneste simuleringResultatTjeneste,
                                  StartSimuleringTjeneste startSimuleringTjenesteFpWsProxy) {
        this.simuleringResultatTjeneste = simuleringResultatTjeneste;
        this.startSimuleringTjenesteFpWsProxy = startSimuleringTjenesteFpWsProxy;
    }

    @POST
    @Path("resultat")
    @Operation(description = "Hent resultat av simulering mot økonomi", summary = ("Returnerer simuleringsresultat."), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public SimuleringResultatDto hentSimuleringResultat(@Valid BehandlingIdDto behandlingIdDto) {
        Optional<SimuleringResultatDto> optionalSimuleringResultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingIdDto.getBehandlingId());
        return optionalSimuleringResultatDto.orElse(null);
    }

    @POST
    @Path("resultat-uten-inntrekk")
    @Operation(description = "Hent detaljert resultat av simulering mot økonomi med og uten inntrekk", summary = ("Returnerer simuleringsresultat."), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public SimuleringDto hentSimuleringResultatMedOgUtenInntrekk(@Valid BehandlingIdDto behandlingIdDto) {
        Optional<SimuleringDto> optionalSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingIdDto.getBehandlingId());
        return optionalSimuleringDto.orElse(null);
    }

    @Deprecated
    @POST
    @Path("start/v2")
    @Operation(description = "Start simulering for behandling med oppdrag via fpwsproxy", summary = ("Returnerer status på om oppdrag er gyldig"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response startSimuleringViaFpwsproxy(@TilpassetAbacAttributt(supplierClass = OppdragskontrollDtoAbacSupplier.Supplier.class) @Valid OppdragskontrollDto oppdragskontrollDto) {
        return startSimulering(oppdragskontrollDto);
    }

    @POST
    @Path("start")
    @Operation(description = "Start simulering for behandling med oppdrag via fpwsproxy", summary = ("Returnerer status på om oppdrag er gyldig"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response startSimulering(@TilpassetAbacAttributt(supplierClass = OppdragskontrollDtoAbacSupplier.Supplier.class) @Valid OppdragskontrollDto oppdragskontrollDto) {
        startSimuleringTjenesteFpWsProxy.startSimulering(oppdragskontrollDto);
        return Response.ok().build();
    }

    @POST
    @Path("kanseller")
    @Operation(description = "Kanseller simulering for behandling", summary = ("Deaktiverer simuleringgrunnlag for behandling"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response kansellerSimulering(@Valid BehandlingIdDto behandlingIdDto) {
        startSimuleringTjenesteFpWsProxy.kansellerSimulering(behandlingIdDto.getBehandlingId());
        return Response.ok().build();
    }

    @POST
    @Path("feilutbetalte-perioder")
    @Operation(description = "Hent sum feilutbetaling og simulerte perioder som er feilutbetalte og kan kreves tilbake fra brukeren.", summary = ("Returnerer perioder som er feilutbetalt."), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioderForTilbakekreving(@Valid BehandlingIdDto behandlingIdDto) {
        return simuleringResultatTjeneste.hentFeilutbetaltePerioder(behandlingIdDto.getBehandlingId());
    }
}
