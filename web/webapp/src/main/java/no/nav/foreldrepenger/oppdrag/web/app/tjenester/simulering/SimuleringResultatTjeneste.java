package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.BeregningResultat;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.FeilutbetalingTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Oppsummering;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimulertBeregningResultat;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
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

    public Optional<SimuleringDto> hentDetaljertSimuleringsResultat(Long behandlingId) {
        Optional<SimulertBeregningResultat> simulertBeregningResultat = hentResultat(behandlingId);
        if (skalKunViseResultatUtenInntrekk(simulertBeregningResultat)) {
            return Optional.of(SimuleringResultatMapper.map(hentNavnTjeneste, brukKunResultatUtenInntrekk(simulertBeregningResultat.get()), true));
        }
        return simulertBeregningResultat.map(resultat -> SimuleringResultatMapper.map(hentNavnTjeneste, resultat, false));
    }

    private boolean skalKunViseResultatUtenInntrekk(Optional<SimulertBeregningResultat> simulertBeregningResultat) {
        return simulertBeregningResultat.isPresent()
                && erBådeInntrekkOgFeilutbetaling(simulertBeregningResultat.get())
                && simulertBeregningResultat.get().getBeregningResultatUtenInntrekk().isPresent();
    }

    public FeilutbetaltePerioderDto hentFeilutbetaltePerioder(Long behandlingId) {
        Optional<SimuleringGrunnlag> optSimuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(behandlingId);
        if (!optSimuleringGrunnlag.isPresent()) {
            throw new TekniskException("FPO-319832", String.format("Fant ikke simuleringsresultat for behandlingId=%s", behandlingId));
        }
        SimuleringGrunnlag simuleringGrunnlag = optSimuleringGrunnlag.get();
        return FeilutbetalingTjeneste.finnFeilutbetaltePerioderForForeldrepengerOgEngangsstønad(simuleringGrunnlag)
                .orElseThrow(() -> new TekniskException("FPO-216725", String.format("Fant ingen perioder med feilutbetaling for bruker, behandlingId=%s", behandlingId)));
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
