package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.KontraktMottakerType;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.PeriodeDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;

public class DetaljertSimuleringResultatDtoBuilder {
    private PeriodeDto periode;
    private Long sumEtterbetaling;
    private Long sumFeilutbetaling;
    private Long sumInntrekk;
    private boolean ingenPerioderMedAvvik = true;
    private List<SimuleringDto.SimuleringForMottakerDto> perioderPerMottaker = new ArrayList<>();


    public DetaljertSimuleringResultatDtoBuilder medPerioderForMottaker(SimuleringDto.SimuleringForMottakerDto simuleringForMottakerDto) {
        this.perioderPerMottaker.add(simuleringForMottakerDto);
        return this;
    }

    public DetaljertSimuleringResultatDtoBuilder medSumEtterbetaling(BigDecimal beløp) {
        this.sumEtterbetaling = beløp != null ? beløp.longValue() : null;
        return this;
    }

    public DetaljertSimuleringResultatDtoBuilder medSumFeilutbetaling(BigDecimal beløp) {
        this.sumFeilutbetaling = beløp != null ? beløp.longValue() : null;
        return this;
    }

    public DetaljertSimuleringResultatDtoBuilder medPeriode(LocalDate fom, LocalDate tom) {
        this.periode = new PeriodeDto(fom, tom);
        return this;
    }

    public DetaljertSimuleringResultatDtoBuilder medInntrekk(BigDecimal inntrekk) {
        this.sumInntrekk = inntrekk != null ? inntrekk.longValue() : null;
        return this;
    }

    public DetaljertSimuleringResultatDtoBuilder medIngenPerioderMedAvvik(boolean ingenPerioderMedAvvik) {
        this.ingenPerioderMedAvvik = ingenPerioderMedAvvik;
        return this;
    }

    public SimuleringDto.DetaljertSimuleringResultatDto build() {
        if (this.periode == null) {
            this.periode = new PeriodeDto(null, null);
        }
        this.perioderPerMottaker.sort((o1, o2) -> KontraktMottakerType.BRUKER.equals(o1.mottakerType()) ? -1 : 1);
        return new SimuleringDto.DetaljertSimuleringResultatDto(this.periode, this.ingenPerioderMedAvvik, this.sumEtterbetaling, this.sumFeilutbetaling, this.sumInntrekk,
            this.perioderPerMottaker);
    }
}
