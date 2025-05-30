package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.util.function.Function;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.StartSimuleringRequest;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.request.SimuleringResultatRequest;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.StartSimuleringTjeneste;
import no.nav.foreldrepenger.oppdrag.web.server.jetty.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;


@RequestScoped
@Path("simulering")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class SimuleringRestTjeneste {

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
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public SimuleringResultatDto hentSimuleringResultat(@TilpassetAbacAttributt(supplierClass = SimuleringResultatRequestAbacDataSupplier.class) @Valid SimuleringResultatRequest behandlingIdDto) {
        var optionalSimuleringResultatDto = simuleringResultatTjeneste.hentResultatFraSimulering(behandlingIdDto.behandlingId());
        return optionalSimuleringResultatDto.orElse(null);
    }

    @POST
    @Path("resultat-uten-inntrekk")
    @Operation(description = "Hent detaljert resultat av simulering mot økonomi med og uten inntrekk", summary = ("Returnerer simuleringsresultat."), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public SimuleringDto hentSimuleringResultatMedOgUtenInntrekk(@TilpassetAbacAttributt(supplierClass = SimuleringResultatRequestAbacDataSupplier.class) @Valid SimuleringResultatRequest behandlingIdDto) {
        var optionalSimuleringDto = simuleringResultatTjeneste.hentDetaljertSimuleringsResultat(behandlingIdDto.behandlingId());
        return optionalSimuleringDto.orElse(null);
    }

    @POST
    @Path("start-v2")
    @Operation(description = "Start simulering for behandling med oppdrag via fpwsproxy", summary = ("Returnerer status på om oppdrag er gyldig"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    public Response startSimulering(@TilpassetAbacAttributt(supplierClass = StartSimuleringRequestDtoAbacSupplier.class) @Valid StartSimuleringRequest oppdragskontrollDto) {
        startSimuleringTjenesteFpWsProxy.startSimulering(oppdragskontrollDto.dto());
        return Response.ok().build();
    }

    @POST
    @Path("kanseller")
    @Operation(description = "Kanseller simulering for behandling", summary = ("Deaktiverer simuleringgrunnlag for behandling"), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.UPDATE, resourceType = ResourceType.FAGSAK, sporingslogg = false)
    public Response kansellerSimulering(@TilpassetAbacAttributt(supplierClass = SimuleringResultatRequestAbacDataSupplier.class) @Valid SimuleringResultatRequest behandlingIdDto) {
        startSimuleringTjenesteFpWsProxy.kansellerSimulering(behandlingIdDto.behandlingId());
        return Response.ok().build();
    }

    @POST
    @Path("feilutbetalte-perioder")
    @Operation(description = "Hent sum feilutbetaling og simulerte perioder som er feilutbetalte og kan kreves tilbake fra brukeren.", summary = ("Returnerer perioder som er feilutbetalt."), tags = "simulering")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK, sporingslogg = true)
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioderForTilbakekreving(@TilpassetAbacAttributt(supplierClass = SimuleringResultatRequestAbacDataSupplier.class) @Valid SimuleringResultatRequest behandlingIdDto) {
        return simuleringResultatTjeneste.hentFeilutbetaltePerioder(behandlingIdDto.behandlingId());
    }

    public static class SimuleringResultatRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (SimuleringResultatRequest) obj;
            return AbacDataAttributter.opprett()
                .leggTil(AppAbacAttributtType.BEHANDLING_ID, req.behandlingId())
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer())
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }
    }

    public static class StartSimuleringRequestDtoAbacSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (StartSimuleringRequest) obj;
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.BEHANDLING_ID, Long.parseLong(req.dto().behandlingId()))
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, req.saksnummer())
                .leggTil(StandardAbacAttributtType.BEHANDLING_UUID, req.behandlingUuid());
        }

    }

}
