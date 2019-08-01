package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.BeregningResultat;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.FeilutbetalingTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Oppsummering;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningResultat;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.SimuleringResultatTjenesteFeil;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;


@ApplicationScoped
public class SimuleringResultatTjenesteImpl implements SimuleringResultatTjeneste {

    private static final String FEATURE_TOGGLE_SLÅ_AV_INNTREKK = "fpsak.slaa-av-inntrekk";
    private SimuleringRepository simuleringRepository;
    private HentNavnTjeneste hentNavnTjeneste;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste;
    private Unleash unleash;

    SimuleringResultatTjenesteImpl() {
        // For CDI
    }

    @Inject
    public SimuleringResultatTjenesteImpl(SimuleringRepository simuleringRepository, HentNavnTjeneste hentNavnTjeneste, SimuleringBeregningTjeneste simuleringBeregningTjeneste, Unleash unleash) {
        this.simuleringRepository = simuleringRepository;
        this.hentNavnTjeneste = hentNavnTjeneste;
        this.simuleringBeregningTjeneste = simuleringBeregningTjeneste;
        this.unleash = unleash;
    }

    @Override
    public Optional<SimuleringResultatDto> hentResultatFraSimulering(Long behandlingId) {
        Optional<SimulertBeregningResultat> simulertBeregningResultat = hentResultat(behandlingId);
        if (simulertBeregningResultat.isPresent()) {
            BeregningResultat beregningResultat;
            boolean slåttAvInntrekk = skalKunViseResultatUtenInntrekk(simulertBeregningResultat);
            if (slåttAvInntrekk) {
                beregningResultat = simulertBeregningResultat.get().getBeregningResultatUtenInntrekk().get();
            } else {
                beregningResultat = simulertBeregningResultat.get().getBeregningResultat();
            }
            return Optional.of(SimuleringResultatDto.builder()
                    .medSumFeilutbetaling(beregningResultat.getOppsummering().getFeilutbetaling())
                    .medSumInntrekk(beregningResultat.getOppsummering().getInntrekkNesteUtbetaling())
                    .medSlåttAvInntrekk(slåttAvInntrekk)
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public Optional<SimuleringDto> hentDetaljertSimuleringsResultat(Long behandlingId) {
        Optional<SimulertBeregningResultat> simulertBeregningResultat = hentResultat(behandlingId);
        if (skalKunViseResultatUtenInntrekk(simulertBeregningResultat)) {
            return Optional.of(SimuleringResultatMapper.map(hentNavnTjeneste, brukKunResultatUtenInntrekk(simulertBeregningResultat.get()), true));
        }
        return simulertBeregningResultat.map(resultat -> SimuleringResultatMapper.map(hentNavnTjeneste, resultat, false));
    }

    private boolean skalKunViseResultatUtenInntrekk(Optional<SimulertBeregningResultat> simulertBeregningResultat) {
        return unleash.isEnabled(FEATURE_TOGGLE_SLÅ_AV_INNTREKK)
                && simulertBeregningResultat.isPresent() && erBådeInntrekkOgFeilutbetaling(simulertBeregningResultat.get())
                && simulertBeregningResultat.get().getBeregningResultatUtenInntrekk().isPresent();
    }

    @Override
    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Long behandlingId) {
        Optional<SimuleringGrunnlag> optSimuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        if (!optSimuleringGrunnlag.isPresent()) {
            throw SimuleringResultatTjenesteFeil.FACTORY.finnesIkkeSimuleringsResultat(behandlingId).toException();
        }
        SimuleringGrunnlag simuleringGrunnlag = optSimuleringGrunnlag.get();
        return FeilutbetalingTjeneste.finnFeilutbetaltePerioderForForeldrepengerOgEngangsstønad(simuleringGrunnlag)
                .orElseThrow(() -> SimuleringResultatTjenesteFeil.FACTORY.finnesIkkeFeilutbetalingsperioderForBruker(behandlingId).toException());
    }

    private Optional<SimulertBeregningResultat> hentResultat(Long behandlingId) {
        Optional<SimuleringGrunnlag> optSimuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        if (optSimuleringGrunnlag.isPresent()) {
            SimuleringGrunnlag simuleringGrunnlag = optSimuleringGrunnlag.get();
            SimulertBeregningResultat resultat = simuleringBeregningTjeneste.hentBeregningsresultatMedOgUtenInntrekk(simuleringGrunnlag);
            return Optional.of(resultat);
        }
        return Optional.empty();
    }

    private boolean erBådeInntrekkOgFeilutbetaling(SimulertBeregningResultat resultat) {
        Oppsummering oppsummering = resultat.getBeregningResultat().getOppsummering();
        BigDecimal inntrekkNesteUtbetaling = oppsummering.getInntrekkNesteUtbetaling() != null ? oppsummering.getInntrekkNesteUtbetaling() : BigDecimal.ZERO;
        BigDecimal feilutbetaling = oppsummering.getFeilutbetaling() != null ? oppsummering.getFeilutbetaling() : BigDecimal.ZERO;
        return inntrekkNesteUtbetaling.compareTo(BigDecimal.ZERO) != 0 && feilutbetaling.compareTo(BigDecimal.ZERO) != 0;
    }

    private SimulertBeregningResultat brukKunResultatUtenInntrekk(SimulertBeregningResultat resultat) {
        return resultat.getBeregningResultatUtenInntrekk()
                .map(br -> new SimulertBeregningResultat(br, resultat.getGjelderYtelseType()))
                .orElseThrow(() -> new IllegalStateException("Skal alltid ha resultat uten inntrekk her"));
    }


}
