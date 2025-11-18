package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.PeriodeDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringDto;
import no.nav.foreldrepenger.kontrakter.simulering.resultat.v1.SimuleringResultatDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.FeilutbetalingTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.vedtak.exception.TekniskException;


@ApplicationScoped
public class SimuleringResultatTjeneste {

    private SimuleringRepository simuleringRepository;
    private PersonTjeneste personTjeneste;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;

    SimuleringResultatTjeneste() {
        // For CDI
    }

    @Inject
    public SimuleringResultatTjeneste(SimuleringRepository simuleringRepository, PersonTjeneste personTjeneste, SimuleringBeregningTjeneste simuleringBeregningTjeneste) {
        this.simuleringRepository = simuleringRepository;
        this.personTjeneste = personTjeneste;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
    }

    public Optional<SimuleringResultatDto> hentResultatFraSimulering(Long behandlingId) {
        return hentResultat(behandlingId).map(SimuleringResultatTjeneste::mapTilSimuleringResultatDto);
    }

    private static SimuleringResultatDto mapTilSimuleringResultatDto(SimulertBeregningResultat resultat) {
        var slåttAvInntrekk = skalKunViseResultatUtenInntrekk(resultat);

        var beregningResultat = resultat.getBeregningResultatUtenInntrekk().filter(r -> slåttAvInntrekk).orElseGet(resultat::getBeregningResultat);

        return lagSimuleringResultatDto(beregningResultat.getOppsummering().getFeilutbetaling(),
            beregningResultat.getOppsummering().getInntrekkNesteUtbetaling(), slåttAvInntrekk);
    }

    private static SimuleringResultatDto lagSimuleringResultatDto(BigDecimal sumFeilutbetaling, BigDecimal sumInntrekk, boolean slåttAvInntrekk) {
        return new SimuleringResultatDto(sumFeilutbetaling != null ? sumFeilutbetaling.longValue() : null,
            sumInntrekk != null ? sumInntrekk.longValue() : null, slåttAvInntrekk);
    }

    public Optional<SimuleringDto> hentDetaljertSimuleringsResultat(Long behandlingId) {
        var simulertBeregningResultat = hentResultat(behandlingId);
        if (simulertBeregningResultat.isPresent() && skalKunViseResultatUtenInntrekk(simulertBeregningResultat.get())) {
            return Optional.of(SimuleringResultatMapper.map(personTjeneste, brukKunResultatUtenInntrekk(simulertBeregningResultat.get()), true));
        }
        return simulertBeregningResultat.map(resultat -> SimuleringResultatMapper.map(personTjeneste, resultat, false));
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
            .map(futp -> lagDto(futp.sumFeilutbetaling(), futp.perioder().stream().map(p -> new PeriodeDto(p.getPeriodeFom(), p.getPeriodeTom())).toList()))
            .orElseThrow(() -> new TekniskException("FPO-216725", String.format("Fant ingen perioder med feilutbetaling for bruker, behandlingId=%s", behandlingId)));
    }

    private static FeilutbetaltePerioderDto lagDto(Long sumFeilutbetaling, List<PeriodeDto> perioder) {
        Objects.requireNonNull(sumFeilutbetaling, "sumFeilutbetaling");
        return new FeilutbetaltePerioderDto(Math.abs(sumFeilutbetaling), perioder);
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
