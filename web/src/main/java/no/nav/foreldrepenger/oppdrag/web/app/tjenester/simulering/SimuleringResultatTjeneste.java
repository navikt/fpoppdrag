package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.FeilutbetalingTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.PeriodeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;
import no.nav.vedtak.exception.TekniskException;


@ApplicationScoped
public class SimuleringResultatTjeneste {

    private SimuleringRepository simuleringRepository;
    private HentNavnTjeneste hentNavnTjeneste;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;

    SimuleringResultatTjeneste() {
        // For CDI
    }

    @Inject
    public SimuleringResultatTjeneste(SimuleringRepository simuleringRepository, HentNavnTjeneste hentNavnTjeneste, SimuleringBeregningTjeneste simuleringBeregningTjeneste) {
        this.simuleringRepository = simuleringRepository;
        this.hentNavnTjeneste = hentNavnTjeneste;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
    }

    public Optional<SimuleringResultatDto> hentResultatFraSimulering(Long behandlingId) {
        return hentResultat(behandlingId).map(SimuleringResultatTjeneste::mapTilSimuleringResultatDto);
    }

    private static SimuleringResultatDto mapTilSimuleringResultatDto(SimulertBeregningResultat resultat) {
        var slåttAvInntrekk = skalKunViseResultatUtenInntrekk(resultat);

        var beregningResultat = resultat.getBeregningResultatUtenInntrekk().filter(r -> slåttAvInntrekk).orElseGet(resultat::getBeregningResultat);

        return new SimuleringResultatDto(beregningResultat.getOppsummering().getFeilutbetaling(),
            beregningResultat.getOppsummering().getInntrekkNesteUtbetaling(), slåttAvInntrekk);
    }

    public Optional<SimuleringDto> hentDetaljertSimuleringsResultat(Long behandlingId) {
        var simulertBeregningResultat = hentResultat(behandlingId);
        if (simulertBeregningResultat.isPresent() && skalKunViseResultatUtenInntrekk(simulertBeregningResultat.get())) {
            return Optional.of(SimuleringResultatMapper.map(hentNavnTjeneste, brukKunResultatUtenInntrekk(simulertBeregningResultat.get()), true));
        }
        return simulertBeregningResultat.map(resultat -> SimuleringResultatMapper.map(hentNavnTjeneste, resultat, false));
    }

    private static boolean skalKunViseResultatUtenInntrekk(SimulertBeregningResultat simulertBeregningResultat) {
        return erBådeInntrekkOgFeilutbetaling(simulertBeregningResultat);
    }

    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Long behandlingId) {
        var optSimuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        if (optSimuleringGrunnlag.isEmpty()) {
            throw new TekniskException("FPO-319832", String.format("Fant ikke simuleringsresultat for behandlingId=%s", behandlingId));
        }
        var simuleringGrunnlag = optSimuleringGrunnlag.get();
        return FeilutbetalingTjeneste.finnFeilutbetaltePerioderForForeldrepengeYtelser(simuleringGrunnlag)
            .map(futp -> FeilutbetaltePerioderDto.lagDto(futp.sumFeilutbetaling(), futp.perioder().stream().map(p -> new PeriodeDto(p.getPeriodeFom(), p.getPeriodeTom())).toList()))
            .orElseThrow(() -> new TekniskException("FPO-216725", String.format("Fant ingen perioder med feilutbetaling for bruker, behandlingId=%s", behandlingId)));
    }

    private Optional<SimulertBeregningResultat> hentResultat(Long behandlingId) {
        var optSimuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        if (optSimuleringGrunnlag.isPresent()) {
            var simuleringGrunnlag = optSimuleringGrunnlag.get();
            var resultat = simuleringBeregningTjeneste.hentBeregningsresultatMedOgUtenInntrekk(simuleringGrunnlag);
            return Optional.of(resultat);
        }
        return Optional.empty();
    }

    private static boolean erBådeInntrekkOgFeilutbetaling(SimulertBeregningResultat resultat) {
        var oppsummering = resultat.getBeregningResultat().getOppsummering();
        var inntrekkNesteUtbetaling = oppsummering.getInntrekkNesteUtbetaling() != null ? oppsummering.getInntrekkNesteUtbetaling() : BigDecimal.ZERO;
        var feilutbetaling = oppsummering.getFeilutbetaling() != null ? oppsummering.getFeilutbetaling() : BigDecimal.ZERO;
        return inntrekkNesteUtbetaling.compareTo(BigDecimal.ZERO) != 0 && feilutbetaling.compareTo(BigDecimal.ZERO) != 0;
    }

    private SimulertBeregningResultat brukKunResultatUtenInntrekk(SimulertBeregningResultat resultat) {
        return resultat.getBeregningResultatUtenInntrekk()
                .map(br -> new SimulertBeregningResultat(br, resultat.getGjelderYtelseType()))
                .orElseThrow(() -> new IllegalStateException("Skal alltid ha resultat uten inntrekk her"));
    }
}
