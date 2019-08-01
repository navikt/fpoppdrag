package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.math.BigDecimal;

import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.Periode;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto.PeriodeDto;

public class SimuleringResultatPerMånedDto {
    private PeriodeDto periode;
    private Long beløp;

    public SimuleringResultatPerMånedDto(Periode periode, BigDecimal beløp) {
        this.periode = new PeriodeDto(periode);
        this.beløp = beløp != null ? beløp.longValue() : null;
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    public Long getBeløp() {
        return beløp;
    }

}
