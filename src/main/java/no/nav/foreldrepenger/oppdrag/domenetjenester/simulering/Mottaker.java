package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;

public record Mottaker(MottakerType mottakerType, String mottakerNummer, LocalDate nesteUtbetalingsperiodeFom, LocalDate nesteUtbetalingsperiodeTom) {

    public Mottaker(MottakerType mottakerType, String mottakerNummer, Periode periode) {
        this(mottakerType, mottakerNummer, periode.getPeriodeFom(), periode.getPeriodeTom());
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mottaker mottaker = (Mottaker) o;
        return Objects.equals(mottakerType, mottaker.mottakerType) &&
                Objects.equals(mottakerNummer, mottaker.mottakerNummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mottakerType, mottakerNummer);
    }
}
