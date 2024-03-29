package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static java.time.temporal.ChronoUnit.DAYS;
import static no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType.FEIL;

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

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimulertPostering;

public class FeilutbetalingTjeneste {

    private FeilutbetalingTjeneste() {
        // Skal ikke instansieres
    }

    public record FeilutbetaltePerioder(Long sumFeilutbetaling, List<Periode> perioder) {}

    public static Optional<FeilutbetaltePerioder> finnFeilutbetaltePerioderForForeldrepengeYtelser(SimuleringGrunnlag simuleringGrunnlag) {
        var fagOmrådeKodeForBruker = Fagområde.utledFra(simuleringGrunnlag.getYtelseType());
        return simuleringGrunnlag.getSimuleringResultat().getSimuleringMottakere()
                .stream()
                .filter(m -> m.getMottakerType().equals(MottakerType.BRUKER))
                .findFirst()
                .map(m -> mapTilFeilutbetaltePerioder(m, fagOmrådeKodeForBruker));
    }

    private static FeilutbetaltePerioder mapTilFeilutbetaltePerioder(SimuleringMottaker mottaker, Fagområde fagOmrådeKodeForBruker) {
        var posteringer = mottaker.getSimulertePosteringerForFeilutbetaling().stream()
                .filter(p -> fagOmrådeKodeForBruker.equals(p.getFagOmrådeKode()))
                .filter(p -> PosteringType.FEIL.equals(p.getPosteringType()))
                .toList();

        var sumFeilutbetaling = SimuleringBeregningTjeneste.beregnFeilutbetaltBeløp(posteringer);

        return new FeilutbetaltePerioder(sumFeilutbetaling.longValue(), finnFeilutbetaltePerioder(posteringer));
    }

    static List<Periode> finnFeilutbetaltePerioder(List<SimulertPostering> feilutbetaltePosteringer) {
        var periodeListe = feilutbetaltePosteringer.stream()
                .map(p -> new Periode(p.getFom(), p.getTom()))
                .toList();

        return slåSammenSammenhengendePerioder(periodeListe);
    }

    static List<Periode> slåSammenSammenhengendePerioder(List<Periode> periodeListe) {
        var sortertListe = periodeListe.stream().sorted(Comparator.comparing(Periode::getPeriodeFom)).toList();
        List<Periode> resultat = new ArrayList<>();
        LocalDate fom = null;
        LocalDate tom = null;
        for (var p : sortertListe) {
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

        var erHverdagerIMellom = førstePeriodeTom.plusDays(1)
                .datesUntil(p.getPeriodeFom())
                .anyMatch(d -> erIkkeHelg(d.getDayOfWeek()));
        return !erHverdagerIMellom;
    }

    public static int finnAntallVirkedager(LocalDate fom, LocalDate tom) {
        var antallVirkedager = 0;
        var dato = fom;
        while (!dato.isAfter(tom)) {
            if (erIkkeHelg(dato.getDayOfWeek())) {
                antallVirkedager++;
            }
            dato = dato.plusDays(1);
        }
        return antallVirkedager;
    }

    static BigDecimal beregnDagsats(SimulertPostering postering) {
        var antallVirkedager = finnAntallVirkedager(postering.getFom(), postering.getTom());
        if (antallVirkedager > 0) {
            return postering.getBeløp().divide(BigDecimal.valueOf(antallVirkedager), RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    static boolean erIkkeHelg(DayOfWeek dag) {
        return !dag.equals(DayOfWeek.SATURDAY) && !dag.equals(DayOfWeek.SUNDAY);
    }


    static Map<YearMonth, List<SimulertPostering>> finnMånederMedFeilutbetaling(List<SimulertPostering> posteringer) {
        var posteringerPerMåned = posteringer.stream()
                .collect(Collectors.groupingBy(p -> YearMonth.from(p.getFom())));

        Map<YearMonth, List<SimulertPostering>> resultat = new HashMap<>();

        for (var entry : posteringerPerMåned.entrySet()) {
            var harFeilutbetaling = entry.getValue().stream().anyMatch(p -> FEIL.equals(p.getPosteringType()));
            if (harFeilutbetaling) {
                resultat.put(entry.getKey(), entry.getValue());
            }
        }
        return resultat;
    }
}
