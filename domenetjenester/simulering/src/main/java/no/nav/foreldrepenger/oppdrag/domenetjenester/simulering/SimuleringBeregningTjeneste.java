package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

@ApplicationScoped
public class SimuleringBeregningTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(SimuleringBeregningTjeneste.class);

    public BeregningResultat hentBeregningsresultat(SimuleringGrunnlag simuleringGrunnlag) {
        return beregnResultat(simuleringGrunnlag, false)
                .orElseThrow(() -> new IllegalStateException("Utvikler-feil: Skal alltid ha beregningsresultat."));
    }

    public SimulertBeregningResultat hentBeregningsresultatMedOgUtenInntrekk(SimuleringGrunnlag simuleringGrunnlag) {
        var simulertBeregningResultat = new SimulertBeregningResultat(hentBeregningsresultat(simuleringGrunnlag), simuleringGrunnlag.getYtelseType());

        var beregningResultatUtenInntrekk = beregnResultat(simuleringGrunnlag, true);
        beregningResultatUtenInntrekk.ifPresent(simulertBeregningResultat::setBeregningResultatUtenInntrekk);
        return simulertBeregningResultat;
    }

    private Optional<BeregningResultat> beregnResultat(SimuleringGrunnlag simuleringGrunnlag, boolean beregnUtenInntrekk) {
        var simuleringResultat = simuleringGrunnlag.getSimuleringResultat();
        if (beregnUtenInntrekk && !harPosteringerUtenInntrekk(simuleringResultat)) {
            return Optional.empty();
        }

        Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat = new HashMap<>();

        for (var simuleringMottaker : simuleringResultat.getSimuleringMottakere()) {
            List<SimulertPostering> simulertePosteringer;
            if (beregnUtenInntrekk && MottakerType.BRUKER.equals(simuleringMottaker.getMottakerType())) {
                simulertePosteringer = simuleringMottaker.getSimulertePosteringerUtenInntrekk();
            } else {
                simulertePosteringer = simuleringMottaker.getSimulertePosteringer();
            }

            var mottaker = new Mottaker(simuleringMottaker.getMottakerType(), simuleringMottaker.getMottakerNummer(),
                finnNesteUtbetalingsperiode(simulertePosteringer, simuleringGrunnlag.getSimuleringKjørtDato()));
            var resultat = beregnPosteringerPerMånedOgFagområde(simulertePosteringer);
            beregningsresultat.put(mottaker, resultat);
        }
        var oppsummering = opprettOppsummering(beregningsresultat, simuleringGrunnlag.getYtelseType());

        return Optional.of(new BeregningResultat(oppsummering, beregningsresultat));
    }

    private static Periode finnNesteUtbetalingsperiode(List<SimulertPostering> simulertePosteringer, LocalDateTime simuleringKjørtDato) {
        var posteringNestePeriode = simulertePosteringer.stream()
                .filter(p -> p.getForfallsdato().isAfter(simuleringKjørtDato.toLocalDate()))
                .max(Comparator.comparing(SimulertPostering::getForfallsdato));

        var yearMonth = posteringNestePeriode.map(p -> YearMonth.from(p.getFom()))
                .orElse(YearMonth.from(simuleringKjørtDato.plusMonths(1)));
        return new Periode(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    private static boolean harPosteringerUtenInntrekk(SimuleringResultat simuleringResultat) {
        return simuleringResultat.getSimuleringMottakere().stream()
                .filter(m -> MottakerType.BRUKER.equals(m.getMottakerType()))
                .anyMatch(m -> !m.getSimulertePosteringerUtenInntrekk().isEmpty());
    }

    List<SimulertBeregningPeriode> beregnPosteringerPerMånedOgFagområde(List<SimulertPostering> posteringer) {
        Objects.requireNonNull(posteringer, "posteringer"); //NOSONAR

        List<SimulertBeregningPeriode> simulerteBeregninger = new ArrayList<>();
        var posteringerPerMåned = grupperPerMåned(posteringer);
        for (var entry : posteringerPerMåned.entrySet()) {
            var periode = finnPeriode(entry.getValue());
            var simulertBeregningBuilder = SimulertBeregningPeriode.builder()
                    .medPeriode(periode);

            var posteringerPerFagområde = grupperPerFagområde(entry.getValue());
            for (var entryPerFagomr : posteringerPerFagområde.entrySet()) {
                var simulertBeregning = beregnPosteringerPerFagområde(entryPerFagomr.getValue());
                simulertBeregningBuilder.medBeregning(entryPerFagomr.getKey(), simulertBeregning)
                        .leggTilPåResultat(simulertBeregning.getResultat())
                        .leggTilPåInntrekkNesteMåned(simulertBeregning.getMotregning())
                        .leggTilPåResultatEtterMotregning(simulertBeregning.getDifferanse());
            }
            simulerteBeregninger.add(simulertBeregningBuilder.build());
        }
        return simulerteBeregninger;
    }

    Oppsummering opprettOppsummering(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat, YtelseType ytelseType) {
        var oppsummering = new Oppsummering();

        oppsummering.setPeriodeFom(finnOppsummertPeriodeFom(beregningsresultat));
        oppsummering.setPeriodeTom(finnOppsummertPeriodeTom(beregningsresultat));

        var fagOmrådeKode = Fagområde.utledFra(ytelseType);

        if (ytelseType.erIkkeEngangsstønad()) {
            oppsummering.setInntrekkNesteUtbetaling(finnInntrekk(beregningsresultat, fagOmrådeKode));
        }

        var beregninger = finnBeregningerForBrukerForFagområde(
                fagOmrådeKode,
                beregningsresultat);

        //En netto reduksjon i feilutbetaling skal ikke vises som en feilutbetaling.
        //Derfor settes feilutbetaling til 0 i dette tilfellet
        var feilutbetaling = beregninger.stream().map(SimulertBeregning::getFeilutbetaltBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);
        var erReduksjonIFeilutbetaling = feilutbetaling.signum() == 1;
        oppsummering.setFeilutbetaling(erReduksjonIFeilutbetaling ? BigDecimal.ZERO : feilutbetaling);

        var etterbetaling = beregninger.stream().map(SimulertBeregning::getEtterbetaling).reduce(BigDecimal.ZERO, BigDecimal::add);
        oppsummering.setEtterbetaling(etterbetaling);
        return oppsummering;
    }

    BigDecimal finnInntrekk(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat, Fagområde fagOmrådeKode) {
        return finnInntrekkSum(beregningsresultat, fagOmrådeKode);
    }

    private static BigDecimal finnInntrekkSum(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat, Fagområde fagOmrådeKode) {
        var mottakerBrukerOpt = beregningsresultat.keySet().stream().filter(m -> m.mottakerType().equals(MottakerType.BRUKER)).findFirst();
        if (mottakerBrukerOpt.isPresent()) {
            var mottaker = mottakerBrukerOpt.get();
            var perioder = beregningsresultat.get(mottaker);
            return finnInntrekkSum(perioder, fagOmrådeKode);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal finnInntrekkSum(List<SimulertBeregningPeriode> perioder, Fagområde fagOmrådeKode) {
        return perioder.stream()
                .map(p -> p.getBeregningPerFagområde().get(fagOmrådeKode))
                .filter(Objects::nonNull)
                .map(SimulertBeregning::getMotregning)
                .filter(p -> p.signum() == -1)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<SimulertBeregning> finnBeregningerForBrukerForFagområde(Fagområde fagOmrådeKode,
                                                                                Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat) {
        var mottakerBrukerOpt = beregningsresultat.keySet().stream().filter(m -> m.mottakerType().equals(MottakerType.BRUKER)).findFirst();
        if (mottakerBrukerOpt.isPresent()) {
            var mottakerBruker = mottakerBrukerOpt.get();
            var nesteUtbetalingsperiode = YearMonth.from(mottakerBruker.nesteUtbetalingsperiodeFom());
            var resultatForBruker = beregningsresultat.get(mottakerBruker);
            return resultatForBruker.stream()
                    .filter(p -> erIkkeNesteUtbetalingsperiode(p.getPeriode(), nesteUtbetalingsperiode))
                    .filter(r -> r.getBeregningPerFagområde().containsKey(fagOmrådeKode))
                    .map(r -> r.getBeregningPerFagområde().get(fagOmrådeKode))
                    .toList();
        }
        return Collections.emptyList();
    }

    private static LocalDate finnOppsummertPeriodeTom(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat) {
        LocalDate sisteTomDato = null;
        for (var entry : beregningsresultat.entrySet()) {
            var mottaker = entry.getKey();
            var nesteUtbetalingsperiode = YearMonth.from(mottaker.nesteUtbetalingsperiodeFom());
            var tomDato = entry.getValue().stream()
                    .filter(p -> erIkkeNesteUtbetalingsperiode(p.getPeriode(), nesteUtbetalingsperiode))
                    .map(b -> b.getPeriode().getPeriodeTom())
                    .reduce((a, b) -> a.isAfter(b) ? a : b);
            if (tomDato.isPresent()) {
                sisteTomDato = sisteTomDato == null || tomDato.get().isAfter(sisteTomDato) ? tomDato.get() : sisteTomDato;
            }
        }
        return sisteTomDato;
    }

    private static LocalDate finnOppsummertPeriodeFom(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat) {
        LocalDate førsteFomDato = null;
        for (var entry : beregningsresultat.entrySet()) {
            var mottaker = entry.getKey();
            var nesteUtbetalingsperiode = YearMonth.from(mottaker.nesteUtbetalingsperiodeFom());
            var fomDato = entry.getValue().stream()
                    .filter(p -> erIkkeNesteUtbetalingsperiode(p.getPeriode(), nesteUtbetalingsperiode))
                    .map(b -> b.getPeriode().getPeriodeFom())
                    .reduce((a, b) -> a.isBefore(b) ? a : b);
            if (fomDato.isPresent()) {
                førsteFomDato = førsteFomDato == null || fomDato.get().isBefore(førsteFomDato) ? fomDato.get() : førsteFomDato;
            }
        }
        return førsteFomDato;
    }

    private static boolean erIkkeNesteUtbetalingsperiode(Periode periode, YearMonth nesteUtbetalingsperiode) {
        return periode == null || !YearMonth.from(periode.getPeriodeFom()).equals(nesteUtbetalingsperiode);
    }

    private static Periode finnPeriode(List<SimulertPostering> posteringer) {
        if (posteringer.isEmpty()) {
            throw new IllegalArgumentException("Utvikler-feil, listen skal ikke være tom");
        }

        return new Periode(
                posteringer.stream().map(SimulertPostering::getFom).reduce((a, b) -> a.isBefore(b) ? a : b).get(), //NOSONAR
                posteringer.stream().map(SimulertPostering::getTom).reduce((a, b) -> a.isAfter(b) ? a : b).get()   //NOSONAR
        );
    }

    private SimulertBeregning beregnPosteringerPerFagområde(List<SimulertPostering> posteringer) {
        return beregn(posteringer);
    }

    private static SimulertBeregning beregn(List<SimulertPostering> posteringer) {

        var feilutbetalingPosteringer = bareFeilutbetalingPosteringer(posteringer); // 1 FEIL
        var feilutbetaltBeløp = summerBeløp(feilutbetalingPosteringer); // 6381

        var tidligereUtbetaltBeløp = beregnTidligereUtbetaltBeløp(posteringer, feilutbetaltBeløp); // 6381

        var nyttBeløp = beregnNyttBeløp(posteringer, feilutbetaltBeløp); // 0
        var nyttMinusUtbetalt = nyttBeløp.subtract(tidligereUtbetaltBeløp); // -6381
        var motregning = beregnMotregning(posteringer); // 0
        var resultatUtenFeilutbetaling = nyttMinusUtbetalt.add(motregning); // -6381
        var resultat = feilutbetalingPosteringer.isEmpty() ? resultatUtenFeilutbetaling : feilutbetaltBeløp.negate(); // -6381
        var etterbetaling = utledEtterbetaling(feilutbetalingPosteringer, resultatUtenFeilutbetaling); // 0

        sanityCheckResultater(feilutbetalingPosteringer, feilutbetaltBeløp);

        return SimulertBeregning.builder()
                .medTidligereUtbetaltBeløp(tidligereUtbetaltBeløp)
                .medNyttBeregnetBeløp(nyttBeløp)
                .medDifferanse(nyttMinusUtbetalt)
                .medFeilutbetaltBeløp(feilutbetaltBeløp.negate()) //for at positive feilutbetalinger vises med negativt fortegn i GUI
                .medMotregning(motregning)
                .medEtterbetaling(etterbetaling)
                .medResultat(resultat)
                .build();
    }

    private static BigDecimal utledEtterbetaling(List<SimulertPostering> feilutbetalingPosteringer, BigDecimal resultatUtenFeilutbetaling) {
        var etterbetaling = feilutbetalingPosteringer.isEmpty() && resultatUtenFeilutbetaling.signum() == 1 ? resultatUtenFeilutbetaling : BigDecimal.ZERO;
        if (etterbetaling.signum() == -1) {
            //Dersom tilbakeførte trekk dekker opp en feilutbetaling vil det ikke finnes en postering for feilutbetaling.
            //Men ny ytelse vil da være mindre enn utbetalt. Etterbetalingen blir da negativ. Man skal da sette etterbetalingen til 0.
            return BigDecimal.ZERO;
        } else {
            return etterbetaling;
        }
    }

    private static void sanityCheckResultater(List<SimulertPostering> feilutbetalingPosteringer, BigDecimal feilutbetaltBeløp) {
        if (!feilutbetalingPosteringer.isEmpty() && feilutbetaltBeløp.signum() == 0) {
            LOG.info("FPO-723664: Har FEIL-posteringer i en måned og summen var 0. Dette er ikke forventet at skjer, bør analyseres.");
        }
    }

    private static BigDecimal summerBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .map(p -> BetalingType.K.equals(p.getBetalingType()) ? p.getBeløp().negate() : p.getBeløp())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static List<SimulertPostering> bareFeilutbetalingPosteringer(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.FEIL.equals(p.getPosteringType()))
                .toList();
    }

    static BigDecimal beregnMotregning(List<SimulertPostering> posteringer) {
        return posteringer.stream().filter(p -> PosteringType.JUST.equals(p.getPosteringType()))
                .map(p -> BetalingType.D.equals(p.getBetalingType()) ? p.getBeløp() : p.getBeløp().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static BigDecimal beregnFeilutbetaltBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.FEIL.equals(p.getPosteringType()))
                .filter(p -> BetalingType.D.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    static BigDecimal beregnTidligereUtbetaltBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.YTEL.equals(p.getPosteringType()))
                .filter(p -> BetalingType.K.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    static BigDecimal beregnNyttBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.YTEL.equals(p.getPosteringType()))
                .filter(p -> BetalingType.D.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal beregnNyttBeløp(List<SimulertPostering> posteringer, BigDecimal sumFeilutbetalingPosteringer) {
        var sumPosteringer = summerPosteringer(posteringer, BetalingType.D);
        return sumFeilutbetalingPosteringer.signum() == 1
                ? sumPosteringer.subtract(sumFeilutbetalingPosteringer)
                : sumPosteringer;
    }

    private static BigDecimal beregnTidligereUtbetaltBeløp(List<SimulertPostering> posteringer, BigDecimal sumFeilutbetalingPosteringer) {
        var sumPosteringer = summerPosteringer(posteringer, BetalingType.K); // 6381
        return sumFeilutbetalingPosteringer.signum() == -1
                ? sumPosteringer.add(sumFeilutbetalingPosteringer)
                : sumPosteringer;
    }

    private static BigDecimal summerPosteringer(List<SimulertPostering> posteringer, BetalingType betalingType) {
        return posteringer.stream()
                .filter(p -> PosteringType.YTEL.equals(p.getPosteringType()))
                .filter(p -> betalingType.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static Map<Fagområde, List<SimulertPostering>> grupperPerFagområde(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .collect(Collectors.groupingBy(SimulertPostering::getFagOmrådeKode));
    }

    private static Map<YearMonth, List<SimulertPostering>> grupperPerMåned(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .collect(Collectors.groupingBy(p -> YearMonth.from(p.getFom())));
    }
}
