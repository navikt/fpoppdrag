package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.oppdrag.kodeverk.MottakerType;

public class DetaljertSimuleringResultatDto {

    private LocalDate periodeFom;
    private LocalDate periodeTom;

    private Long sumEtterbetaling;
    private Long sumFeilutbetaling;
    private Long sumInntrekk;
    private boolean ingenPerioderMedAvvik = true;

    private List<SimuleringForMottakerDto> perioderPerMottaker = new ArrayList<>();

    public List<SimuleringForMottakerDto> getPerioderPerMottaker() {
        return Collections.unmodifiableList(perioderPerMottaker);
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public Long getSumEtterbetaling() {
        return sumEtterbetaling;
    }

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public Long getSumInntrekk() {
        return sumInntrekk;
    }

    public boolean isIngenPerioderMedAvvik() {
        return ingenPerioderMedAvvik;
    }

    public static class Builder {
        private DetaljertSimuleringResultatDto kladd = new DetaljertSimuleringResultatDto();

        public Builder medPerioderForMottaker(SimuleringForMottakerDto simuleringForMottakerDto) {
            kladd.perioderPerMottaker.add(simuleringForMottakerDto);
            return this;
        }

        public Builder medSumEtterbetaling(BigDecimal beløp) {
            kladd.sumEtterbetaling = beløp != null ? beløp.longValue() : null;
            return this;
        }

        public Builder medSumFeilutbetaling(BigDecimal beløp) {
            kladd.sumFeilutbetaling = beløp != null ? beløp.longValue() : null;
            return this;
        }

        public Builder medPeriode(LocalDate fom, LocalDate tom) {
            kladd.periodeFom = fom;
            kladd.periodeTom = tom;
            return this;
        }

        public Builder medInntrekk(BigDecimal inntrekk) {
            kladd.sumInntrekk = inntrekk != null ? inntrekk.longValue() : null;
            return this;
        }

        public Builder medIngenPerioderMedAvvik(boolean ingenPerioderMedAvvik) {
            kladd.ingenPerioderMedAvvik = ingenPerioderMedAvvik;
            return this;
        }

        public DetaljertSimuleringResultatDto build() {
            kladd.perioderPerMottaker.sort((o1, o2) -> o1.getMottakerType().equals(MottakerType.BRUKER) ? -1 : 1);
            return kladd;
        }
    }
}
