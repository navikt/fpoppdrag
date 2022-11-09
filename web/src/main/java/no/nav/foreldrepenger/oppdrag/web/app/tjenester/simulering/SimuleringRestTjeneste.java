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
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.StartSimuleringTjenesteFpWsProxy;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.BehandlingIdDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.OppdragskontrollDtoAbacSupplier;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimulerOppdragDto;
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
    private StartSimuleringTjeneste startSimuleringTjeneste;
    private StartSimuleringTjenesteFpWsProxy startSimuleringTjenesteFpWsProxy;

    public SimuleringRestTjeneste() {
        // For CDI
    }

    @Inject
    public SimuleringRestTjeneste(SimuleringResultatTjeneste simuleringResultatTjeneste,
                                  StartSimuleringTjeneste startSimuleringTjeneste,
                                  StartSimuleringTjenesteFpWsProxy startSimuleringTjenesteFpWsProxy) {
        this.simuleringResultatTjeneste = simuleringResultatTjeneste;
        this.startSimuleringTjeneste = startSimuleringTjeneste;
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

    /**
     * NB! Dette endepunktet er under test og vil ikke lagre simuleringsgrunnlag i databasen.
     * Denne kjøres bare etter den andre er kjørt (og det foreligger et simuleringsgrunnlag) og resultatet fra
     * simuleringen med direkte integrasjon mot oppdragssystemet vil bli sammenlignet med det fra fp-ws-proxy!
     * @param oppdragskontrollDto
     * @return tom 200 respons
     */
    @POST
    @Path("start/v2")
    @Operation(description = "Start simulering for behandling med oppdrag via fp-ws-proxy og sammenlinger resultat med direkte integrasjon mot oppdragssystemet", summary = ("Returnerer status på om oppdrag er gyldig"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response startSimuleringViaFpWsProxyFailSafe(@TilpassetAbacAttributt(supplierClass = OppdragskontrollDtoAbacSupplier.Supplier.class) @Valid OppdragskontrollDto oppdragskontrollDto) {
        try {
            startSimuleringTjenesteFpWsProxy.startSimulering(oppdragskontrollDto);
        } catch (Exception e) {
            LOG.info("Noe gikk galt med simulering av oppdrag via fp-ws-proxy", e);
        }
        return Response.ok().build();
    }

    @POST
    @Path("start")
    @Operation(description = "Start simulering for behandling med oppdrag", summary = ("Returnerer status på om oppdrag er gyldig"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response startSimulering(@Valid SimulerOppdragDto simulerOppdragDto) {
        final Long behandlingId = simulerOppdragDto.getBehandlingId();
        startSimuleringTjeneste.startSimulering(behandlingId, simulerOppdragDto.getOppdragPrMottakerDecoded());
        return Response.ok().build();
    }

    @POST
    @Path("kanseller")
    @Operation(description = "Kanseller simulering for behandling", summary = ("Deaktiverer simuleringgrunnlag for behandling"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK)
    public Response kansellerSimulering(@Valid BehandlingIdDto behandlingIdDto) {
        startSimuleringTjeneste.kansellerSimulering(behandlingIdDto.getBehandlingId());
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
