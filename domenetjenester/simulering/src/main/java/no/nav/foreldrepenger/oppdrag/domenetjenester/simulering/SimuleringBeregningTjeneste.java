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

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringResultat;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

@ApplicationScoped
public class SimuleringBeregningTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(SimuleringBeregningTjeneste.class);

    BeregningResultat hentBeregningsresultat(SimuleringGrunnlag simuleringGrunnlag) {
        return beregnResultat(simuleringGrunnlag, false)
                .orElseThrow(() -> new IllegalStateException("Utvikler-feil: Skal alltid ha beregningsresultat."));
    }

    public SimulertBeregningResultat hentBeregningsresultatMedOgUtenInntrekk(SimuleringGrunnlag simuleringGrunnlag) {
        SimulertBeregningResultat simulertBeregningResultat = new SimulertBeregningResultat(hentBeregningsresultat(simuleringGrunnlag), simuleringGrunnlag.getYtelseType());

        Optional<BeregningResultat> beregningResultatUtenInntrekk = beregnResultat(simuleringGrunnlag, true);
        beregningResultatUtenInntrekk.ifPresent(simulertBeregningResultat::setBeregningResultatUtenInntrekk);
        return simulertBeregningResultat;
    }

    private Optional<BeregningResultat> beregnResultat(SimuleringGrunnlag simuleringGrunnlag, boolean beregnUtenInntrekk) {
        SimuleringResultat simuleringResultat = simuleringGrunnlag.getSimuleringResultat();
        if (beregnUtenInntrekk && !harPosteringerUtenInntrekk(simuleringResultat)) {
            return Optional.empty();
        }

        Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat = new HashMap<>();

        for (SimuleringMottaker simuleringMottaker : simuleringResultat.getSimuleringMottakere()) {
            Mottaker mottaker = new Mottaker(simuleringMottaker.getMottakerType(), simuleringMottaker.getMottakerNummer());
            List<SimulertPostering> simulertePosteringer;
            if (beregnUtenInntrekk && MottakerType.BRUKER.equals(simuleringMottaker.getMottakerType())) {
                simulertePosteringer = simuleringMottaker.getSimulertePosteringerUtenInntrekk();
            } else {
                simulertePosteringer = simuleringMottaker.getSimulertePosteringer();
            }

            mottaker.setNesteUtbetalingsperiode(finnNesteUtbetalingsperiode(simulertePosteringer, simuleringGrunnlag.getSimuleringKjørtDato()));
            List<SimulertBeregningPeriode> resultat = beregnPosteringerPerMånedOgFagområde(simulertePosteringer);
            beregningsresultat.put(mottaker, resultat);
        }
        Oppsummering oppsummering = opprettOppsummering(beregningsresultat, simuleringGrunnlag.getYtelseType());

        return Optional.of(new BeregningResultat(oppsummering, beregningsresultat));
    }

    private static Periode finnNesteUtbetalingsperiode(List<SimulertPostering> simulertePosteringer, LocalDateTime simuleringKjørtDato) {
        Optional<SimulertPostering> posteringNestePeriode = simulertePosteringer.stream()
                .filter(p -> p.getForfallsdato().isAfter(simuleringKjørtDato.toLocalDate()))
                .max(Comparator.comparing(SimulertPostering::getForfallsdato));

        YearMonth yearMonth = posteringNestePeriode.map(p -> YearMonth.from(p.getFom()))
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
        Map<YearMonth, List<SimulertPostering>> posteringerPerMåned = grupperPerMåned(posteringer);
        for (Map.Entry<YearMonth, List<SimulertPostering>> entry : posteringerPerMåned.entrySet()) {
            Periode periode = finnPeriode(entry.getValue());
            SimulertBeregningPeriode.Builder simulertBeregningBuilder = SimulertBeregningPeriode.builder()
                    .medPeriode(periode);

            Map<FagOmrådeKode, List<SimulertPostering>> posteringerPerFagområde = grupperPerFagområde(entry.getValue());
            for (Map.Entry<FagOmrådeKode, List<SimulertPostering>> entryPerFagomr : posteringerPerFagområde.entrySet()) {
                SimulertBeregning simulertBeregning = beregnPosteringerPerFagområde(entryPerFagomr.getValue());
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
        Oppsummering oppsummering = new Oppsummering();

        oppsummering.setPeriodeFom(finnOppsummertPeriodeFom(beregningsresultat));
        oppsummering.setPeriodeTom(finnOppsummertPeriodeTom(beregningsresultat));

        FagOmrådeKode fagOmrådeKode = FagOmrådeKode.getFagOmrådeKodeForBrukerForYtelseType(ytelseType);

        if (!ytelseType.gjelderEngangsstønad()) {
            oppsummering.setInntrekkNesteUtbetaling(finnInntrekk(beregningsresultat, fagOmrådeKode));
        }

        List<SimulertBeregning> beregninger = finnBeregningerForBrukerForFagområde(
                fagOmrådeKode,
                beregningsresultat);

        //En netto reduksjon i feilutbetaling skal ikke vises som en feilutbetaling.
        //Derfor settes feilutbetaling til 0 i dette tilfellet
        BigDecimal feilutbetaling = beregninger.stream().map(SimulertBeregning::getFeilutbetaltBeløp).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean erReduksjonIFeilutbetaling = feilutbetaling.signum() == 1;
        oppsummering.setFeilutbetaling(erReduksjonIFeilutbetaling ? BigDecimal.ZERO : feilutbetaling);

        BigDecimal etterbetaling = beregninger.stream().map(SimulertBeregning::getEtterbetaling).reduce(BigDecimal.ZERO, BigDecimal::add);
        oppsummering.setEtterbetaling(etterbetaling);
        return oppsummering;
    }

    BigDecimal finnInntrekk(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat, FagOmrådeKode fagOmrådeKode) {
        return finnInntrekkSum(beregningsresultat, fagOmrådeKode);
    }

    private static BigDecimal finnInntrekkSum(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat, FagOmrådeKode fagOmrådeKode) {
        Optional<Mottaker> mottakerBrukerOpt = beregningsresultat.keySet().stream().filter(m -> m.getMottakerType().equals(MottakerType.BRUKER)).findFirst();
        if (mottakerBrukerOpt.isPresent()) {
            Mottaker mottaker = mottakerBrukerOpt.get();
            List<SimulertBeregningPeriode> perioder = beregningsresultat.get(mottaker);
            return finnInntrekkSum(perioder, fagOmrådeKode);
        }
        return BigDecimal.ZERO;
    }

    private static BigDecimal finnInntrekkSum(List<SimulertBeregningPeriode> perioder, FagOmrådeKode fagOmrådeKode) {
        return perioder.stream()
                .map(p -> p.getBeregningPerFagområde().get(fagOmrådeKode))
                .filter(Objects::nonNull)
                .map(SimulertBeregning::getMotregning)
                .filter(p -> p.signum() == -1)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static List<SimulertBeregning> finnBeregningerForBrukerForFagområde(FagOmrådeKode fagOmrådeKode,
                                                                                Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat) {
        Optional<Mottaker> mottakerBrukerOpt = beregningsresultat.keySet().stream().filter(m -> m.getMottakerType().equals(MottakerType.BRUKER)).findFirst();
        if (mottakerBrukerOpt.isPresent()) {
            Mottaker mottakerBruker = mottakerBrukerOpt.get();
            YearMonth nesteUtbetalingsperiode = YearMonth.from(mottakerBruker.getNesteUtbetalingsperiodeFom());
            List<SimulertBeregningPeriode> resultatForBruker = beregningsresultat.get(mottakerBruker);
            return resultatForBruker.stream()
                    .filter(p -> erIkkeNesteUtbetalingsperiode(p.getPeriode(), nesteUtbetalingsperiode))
                    .filter(r -> r.getBeregningPerFagområde().containsKey(fagOmrådeKode))
                    .map(r -> r.getBeregningPerFagområde().get(fagOmrådeKode))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static LocalDate finnOppsummertPeriodeTom(Map<Mottaker, List<SimulertBeregningPeriode>> beregningsresultat) {
        LocalDate sisteTomDato = null;
        for (var entry : beregningsresultat.entrySet()) {
            Mottaker mottaker = entry.getKey();
            YearMonth nesteUtbetalingsperiode = YearMonth.from(mottaker.getNesteUtbetalingsperiodeFom());
            Optional<LocalDate> tomDato = entry.getValue().stream()
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
            Mottaker mottaker = entry.getKey();
            YearMonth nesteUtbetalingsperiode = YearMonth.from(mottaker.getNesteUtbetalingsperiodeFom());
            Optional<LocalDate> fomDato = entry.getValue().stream()
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

        List<SimulertPostering> feilutbetalingPosteringer = bareFeilutbetalingPosteringer(posteringer); // 1 FEIL
        BigDecimal feilutbetaltBeløp = summerBeløp(feilutbetalingPosteringer); // 6381

        BigDecimal tidligereUtbetaltBeløp = beregnTidligereUtbetaltBeløp(posteringer, feilutbetaltBeløp); // 6381

        BigDecimal nyttBeløp = beregnNyttBeløp(posteringer, feilutbetaltBeløp); // 0
        BigDecimal nyttMinusUtbetalt = nyttBeløp.subtract(tidligereUtbetaltBeløp); // -6381
        BigDecimal motregning = beregnMotregning(posteringer); // 0
        BigDecimal resultatUtenFeilutbetaling = nyttMinusUtbetalt.add(motregning); // -6381
        BigDecimal resultat = feilutbetalingPosteringer.isEmpty() ? resultatUtenFeilutbetaling : feilutbetaltBeløp.negate(); // -6381
        BigDecimal etterbetaling = utledEtterbetaling(feilutbetalingPosteringer, resultatUtenFeilutbetaling); // 0

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
        BigDecimal etterbetaling = feilutbetalingPosteringer.isEmpty() && resultatUtenFeilutbetaling.signum() == 1 ? resultatUtenFeilutbetaling : BigDecimal.ZERO;
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
            LOG.warn("FPO-723664: Har FEIL-posteringer i en måned og summen var 0. Dette er ikke forventet at skjer, bør analyseres.");
        }
    }

    private static BigDecimal summerBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .map(p -> BetalingType.KREDIT.equals(p.getBetalingType()) ? p.getBeløp().negate() : p.getBeløp())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static List<SimulertPostering> bareFeilutbetalingPosteringer(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.FEILUTBETALING.equals(p.getPosteringType()))
                .collect(Collectors.toList());
    }

    static BigDecimal beregnMotregning(List<SimulertPostering> posteringer) {
        return posteringer.stream().filter(p -> PosteringType.JUSTERING.equals(p.getPosteringType()))
                .map(p -> BetalingType.DEBIT.equals(p.getBetalingType()) ? p.getBeløp() : p.getBeløp().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static BigDecimal beregnFeilutbetaltBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.FEILUTBETALING.equals(p.getPosteringType()))
                .filter(p -> BetalingType.DEBIT.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    static BigDecimal beregnTidligereUtbetaltBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.YTELSE.equals(p.getPosteringType()))
                .filter(p -> BetalingType.KREDIT.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    static BigDecimal beregnNyttBeløp(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .filter(p -> PosteringType.YTELSE.equals(p.getPosteringType()))
                .filter(p -> BetalingType.DEBIT.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal beregnNyttBeløp(List<SimulertPostering> posteringer, BigDecimal sumFeilutbetalingPosteringer) {
        BigDecimal sumPosteringer = summerPosteringer(posteringer, PosteringType.YTELSE, BetalingType.DEBIT);
        return sumFeilutbetalingPosteringer.signum() == 1
                ? sumPosteringer.subtract(sumFeilutbetalingPosteringer)
                : sumPosteringer;
    }

    private static BigDecimal beregnTidligereUtbetaltBeløp(List<SimulertPostering> posteringer, BigDecimal sumFeilutbetalingPosteringer) {
        BigDecimal sumPosteringer = summerPosteringer(posteringer, PosteringType.YTELSE, BetalingType.KREDIT); // 6381
        return sumFeilutbetalingPosteringer.signum() == -1
                ? sumPosteringer.add(sumFeilutbetalingPosteringer)
                : sumPosteringer;
    }

    private static BigDecimal summerPosteringer(List<SimulertPostering> posteringer, PosteringType posteringType, BetalingType betalingType) {
        return posteringer.stream()
                .filter(p -> posteringType.equals(p.getPosteringType()))
                .filter(p -> betalingType.equals(p.getBetalingType()))
                .map(SimulertPostering::getBeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private static Map<FagOmrådeKode, List<SimulertPostering>> grupperPerFagområde(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .collect(Collectors.groupingBy(SimulertPostering::getFagOmrådeKode));
    }

    private static Map<YearMonth, List<SimulertPostering>> grupperPerMåned(List<SimulertPostering> posteringer) {
        return posteringer.stream()
                .collect(Collectors.groupingBy(p -> YearMonth.from(p.getFom())));
    }

}
