package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static java.time.temporal.ChronoUnit.DAYS;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEILUTBETALING;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.FeilutbetaltePerioderDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.PeriodeDto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

public class FeilutbetalingTjeneste {

    private FeilutbetalingTjeneste() {
        // Skal ikke instansieres
    }

    public static Optional<FeilutbetaltePerioderDto> finnFeilutbetaltePerioderForForeldrepengerOgEngangsstønad(SimuleringGrunnlag simuleringGrunnlag) {
        Optional<SimuleringMottaker> mottaker = simuleringGrunnlag.getSimuleringResultat().getSimuleringMottakere()
                .stream()
                .filter(m -> m.getMottakerType().equals(MottakerType.BRUKER))
                .findFirst();

        FagOmrådeKode fagOmrådeKodeForBruker = FagOmrådeKode.getFagOmrådeKodeForBrukerForYtelseType(simuleringGrunnlag.getYtelseType());

        if (mottaker.isPresent()) {
            List<SimulertPostering> posteringer = mottaker.get().getSimulertePosteringerForFeilutbetaling().stream()
                    .filter(p -> fagOmrådeKodeForBruker.equals(p.getFagOmrådeKode()))
                    .filter(p -> PosteringType.FEILUTBETALING.equals(p.getPosteringType()))
                    .collect(Collectors.toList());

            List<PeriodeDto> feilutbetaltePerioder = finnFeilutbetaltePerioder(posteringer)
                    .stream().map(PeriodeDto::new).collect(Collectors.toList());
            BigDecimal sumFeilutbetaling = SimuleringBeregningTjeneste.beregnFeilutbetaltBeløp(posteringer);

            return Optional.of(new FeilutbetaltePerioderDto(sumFeilutbetaling.longValue(), feilutbetaltePerioder));
        }
        return Optional.empty();
    }

    static List<Periode> finnFeilutbetaltePerioder(List<SimulertPostering> feilutbetaltePosteringer) {
        List<Periode> periodeListe = feilutbetaltePosteringer.stream()
                .map(p -> new Periode(p.getFom(), p.getTom()))
                .collect(Collectors.toList());

        return slåSammenSammenhengendePerioder(periodeListe);
    }

    static List<Periode> slåSammenSammenhengendePerioder(List<Periode> periodeListe) {
        List<Periode> sortertListe = periodeListe.stream().sorted(Comparator.comparing(Periode::getPeriodeFom)).collect(Collectors.toList());
        List<Periode> resultat = new ArrayList<>();
        LocalDate fom = null;
        LocalDate tom = null;
        for (Periode p : sortertListe) {
            if (fom == null && tom == null) { // Første periode
                fom = p.getPeriodeFom();
                tom = p.getPeriodeTom();
            } else if (erSammenhengendePeriode(tom, p)) {
                tom = p.getPeriodeTom();
            } else {
                resultat.add(new Periode(fom, tom));
                fom = p.getPeriodeFom();
                tom = p.getPeriodeTom();
            }
        }
        if (fom != null && tom != null) {
            resultat.add(new Periode(fom, tom));
        }
        return resultat;
    }

    static boolean erSammenhengendePeriode(LocalDate førstePeriodeTom, Periode p) {
        if (!p.getPeriodeFom().isAfter(førstePeriodeTom) || p.getPeriodeFom().equals(førstePeriodeTom.plusDays(1))) {
            return true;
        }

        if (DAYS.between(førstePeriodeTom.plusDays(1), p.getPeriodeFom()) > 2) {
            return false;
        }

        boolean erHverdagerIMellom = førstePeriodeTom.plusDays(1)
                .datesUntil(p.getPeriodeFom())
                .anyMatch(d -> !erHelg(d.getDayOfWeek()));
        return !erHverdagerIMellom;
    }

    public static int finnAntallVirkedager(LocalDate fom, LocalDate tom) {
        int antallVirkedager = 0;
        LocalDate dato = fom;
        while (!dato.isAfter(tom)) {
            if (!erHelg(dato.getDayOfWeek())) {
                antallVirkedager++;
            }
            dato = dato.plusDays(1);
        }
        return antallVirkedager;
    }

    static BigDecimal beregnDagsats(SimulertPostering postering) {
        int antallVirkedager = finnAntallVirkedager(postering.getFom(), postering.getTom());
        if (antallVirkedager > 0) {
            return postering.getBeløp().divide(BigDecimal.valueOf(antallVirkedager), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    static boolean erHelg(DayOfWeek dag) {
        return dag.equals(DayOfWeek.SATURDAY) || dag.equals(DayOfWeek.SUNDAY);
    }


    static Map<YearMonth, List<SimulertPostering>> finnMånederMedFeilutbetaling(List<SimulertPostering> posteringer) {
        Map<YearMonth, List<SimulertPostering>> posteringerPerMåned = posteringer.stream()
                .collect(Collectors.groupingBy(p -> YearMonth.from(p.getFom())));

        Map<YearMonth, List<SimulertPostering>> resultat = new HashMap<>();

        for (var entry : posteringerPerMåned.entrySet()) {
            boolean harFeilutbetaling = entry.getValue().stream().anyMatch(p -> p.getPosteringType().equals(FEILUTBETALING));
            if (harFeilutbetaling) {
                resultat.put(entry.getKey(), entry.getValue());
            }
        }
        return resultat;
    }
}