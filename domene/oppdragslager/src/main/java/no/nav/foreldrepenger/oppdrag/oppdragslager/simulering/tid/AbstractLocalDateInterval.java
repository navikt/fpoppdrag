package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.tid;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import no.nav.vedtak.konfig.Tid;

/**
 * Basis klasse for modellere et dato interval.
 */
public abstract class AbstractLocalDateInterval implements Comparable<AbstractLocalDateInterval>, Serializable {

    private static final LocalDate TIDENES_BEGYNNELSE = Tid.TIDENES_BEGYNNELSE;
    public static final LocalDate TIDENES_ENDE = Tid.TIDENES_ENDE;

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public abstract LocalDate getFomDato();

    public abstract LocalDate getTomDato();

    protected static LocalDate finnTomDato(LocalDate fom, int antallArbeidsdager) {
        if (antallArbeidsdager < 1) {
            throw new IllegalArgumentException("Antall arbeidsdager må være 1 eller større.");
        }
        LocalDate tom = fom;
        int antallArbeidsdagerTmp = antallArbeidsdager;

        while (antallArbeidsdagerTmp > 0) {
            if (antallArbeidsdagerTmp > antallArbeidsdager) {
                throw new IllegalArgumentException("Antall arbeidsdager beregnes feil.");
            }
            if (erArbeidsdag(tom)) {
                antallArbeidsdagerTmp--;
            }
            if (antallArbeidsdagerTmp > 0) {
                tom = tom.plusDays(1);
            }
        }
        return tom;
    }

    protected static LocalDate finnFomDato(LocalDate tom, int antallArbeidsdager) {
        if (antallArbeidsdager < 1) {
            throw new IllegalArgumentException("Antall arbeidsdager må være 1 eller større.");
        }
        LocalDate fom = tom;
        int antallArbeidsdagerTmp = antallArbeidsdager;

        while (antallArbeidsdagerTmp > 0) {
            if (antallArbeidsdagerTmp > antallArbeidsdager) {
                throw new IllegalArgumentException("Antall arbeidsdager beregnes feil.");
            }
            if (erArbeidsdag(fom)) {
                antallArbeidsdagerTmp--;
            }
            if (antallArbeidsdagerTmp > 0) {
                fom = fom.minusDays(1);
            }
        }
        return fom;
    }

    public static LocalDate forrigeArbeidsdag(LocalDate dato) {
        if (dato != TIDENES_BEGYNNELSE && dato != TIDENES_ENDE) {
            switch (dato.getDayOfWeek()) {
                case SATURDAY:
                    return dato.minusDays(1);
                case SUNDAY:
                    return dato.minusDays(2);
                default:
                    break;
            }
        }
        return dato;
    }

    public static LocalDate nesteArbeidsdag(LocalDate dato) {
        if (dato != TIDENES_BEGYNNELSE && dato != TIDENES_ENDE) {
            switch (dato.getDayOfWeek()) {
                case SATURDAY:
                    return dato.plusDays(2);
                case SUNDAY:
                    return dato.plusDays(1);
                default:
                    break;
            }
        }
        return dato;
    }

    protected static boolean erArbeidsdag(LocalDate dato) {
        return !dato.getDayOfWeek().equals(SATURDAY) && !dato.getDayOfWeek().equals(SUNDAY); // NOSONAR
    }

    @Override
    public int compareTo(AbstractLocalDateInterval periode) {
        return getFomDato().compareTo(periode.getFomDato());
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof AbstractLocalDateInterval)) {
            return false;
        }
        AbstractLocalDateInterval annen = (AbstractLocalDateInterval) object;
        return likFom(annen) && likTom(annen);
    }

    private boolean likFom(AbstractLocalDateInterval annen) {
        boolean likFom = Objects.equals(this.getFomDato(), annen.getFomDato());
        if (this.getFomDato() == null || annen.getFomDato() == null) {
            return likFom;
        }
        return likFom
                || Objects.equals(nesteArbeidsdag(this.getFomDato()), nesteArbeidsdag(annen.getFomDato()));
    }

    private boolean likTom(AbstractLocalDateInterval annen) {
        boolean likTom = Objects.equals(getTomDato(), annen.getTomDato());
        if (this.getTomDato() == null || annen.getTomDato() == null) {
            return likTom;
        }
        return likTom
                || Objects.equals(forrigeArbeidsdag(this.getTomDato()), forrigeArbeidsdag(annen.getTomDato()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFomDato(), getTomDato());
    }

    @Override
    public String toString() {
        return String.format("Periode: %s - %s", getFomDato().format(FORMATTER), getTomDato().format(FORMATTER));
    }
}
