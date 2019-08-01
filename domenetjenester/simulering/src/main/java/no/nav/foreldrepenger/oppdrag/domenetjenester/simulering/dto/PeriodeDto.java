package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto;

import java.time.LocalDate;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Periode;

public class PeriodeDto {
    private LocalDate fom;
    private LocalDate tom;

    public PeriodeDto(Periode periode) {
        this.fom = periode.getPeriodeFom();
        this.tom = periode.getPeriodeTom();
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }
}
