package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;

public class Mottaker {
    private MottakerType mottakerType;
    private String mottakerNummer;
    private LocalDate nesteUtbetalingsperiodeFom;
    private LocalDate nesteUtbetalingsperiodeTom;


    public Mottaker(MottakerType mottakerType, String mottakerNummer) {
        this.mottakerType = mottakerType;
        this.mottakerNummer = mottakerNummer;
    }

    public void setNesteUtbetalingsperiode(Periode periode) {
        this.nesteUtbetalingsperiodeFom = periode.getPeriodeFom();
        this.nesteUtbetalingsperiodeTom = periode.getPeriodeTom();
    }

    public MottakerType getMottakerType() {
        return mottakerType;
    }

    public String getMottakerNummer() {
        return mottakerNummer;
    }

    public LocalDate getNesteUtbetalingsperiodeFom() {
        return nesteUtbetalingsperiodeFom;
    }

    public LocalDate getNesteUtbetalingsperiodeTom() {
        return nesteUtbetalingsperiodeTom;
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
