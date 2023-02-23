package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class Periode {
    private LocalDate periodeFom;
    private LocalDate periodeTom;

    public Periode(YearMonth month) {
        Objects.requireNonNull(month);
        this.periodeFom = month.atDay(1);
        this.periodeTom = month.atEndOfMonth();
    }

    public Periode(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
        this.periodeFom = LocalDate.from(fom);
        this.periodeTom = LocalDate.from(tom);
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Periode)) {
            return false;
        }
        Periode annen = (Periode) o;
        return periodeFom.equals(annen.getPeriodeFom()) && periodeTom.equals(annen.getPeriodeTom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodeFom, periodeTom);
    }
}
