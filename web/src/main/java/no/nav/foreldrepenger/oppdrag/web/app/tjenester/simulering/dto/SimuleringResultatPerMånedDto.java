package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimuleringResultatPerMånedDto(PeriodeDto periode, Long beløp) {

    public SimuleringResultatPerMånedDto(LocalDate fom, LocalDate tom, BigDecimal beløp) {
        this(new PeriodeDto(fom , tom), beløp != null ? beløp.longValue() : null);
    }

}
