package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Oppsummering {
    private LocalDate periodeFom;
    private LocalDate periodeTom;

    private BigDecimal etterbetaling = BigDecimal.ZERO;
    private BigDecimal feilutbetaling = BigDecimal.ZERO;
    private BigDecimal inntrekkNesteUtbetaling;

    void leggTilPåEtterbetaling(BigDecimal beløp) {
        etterbetaling = etterbetaling.add(beløp);
    }

    void leggTilPåFeilutbetaling(BigDecimal beløp) {
        feilutbetaling = feilutbetaling.add(beløp);
    }

    void setInntrekkNesteUtbetaling(BigDecimal beløp) {
        inntrekkNesteUtbetaling = beløp;
    }

    public void setPeriodeFom(LocalDate periodeFom) {
        this.periodeFom = periodeFom;
    }

    public void setPeriodeTom(LocalDate periodeTom) {
        this.periodeTom = periodeTom;
    }

    public BigDecimal getEtterbetaling() {
        return etterbetaling;
    }

    public BigDecimal getFeilutbetaling() {
        return feilutbetaling;
    }

    public BigDecimal getInntrekkNesteUtbetaling() {
        return inntrekkNesteUtbetaling;
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }
}
