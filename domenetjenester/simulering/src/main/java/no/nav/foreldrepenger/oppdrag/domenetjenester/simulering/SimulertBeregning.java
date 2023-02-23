package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;

public class SimulertBeregning {
    private BigDecimal tidligereUtbetaltBeløp = BigDecimal.ZERO;
    private BigDecimal nyttBeregnetBeløp = BigDecimal.ZERO;
    private BigDecimal differanse = BigDecimal.ZERO;
    private BigDecimal resultat = BigDecimal.ZERO;
    private BigDecimal feilutbetaltBeløp = BigDecimal.ZERO;
    private BigDecimal motregning = BigDecimal.ZERO;
    private BigDecimal etterbetaling = BigDecimal.ZERO;

    public BigDecimal getTidligereUtbetaltBeløp() {
        return tidligereUtbetaltBeløp;
    }

    public BigDecimal getNyttBeregnetBeløp() {
        return nyttBeregnetBeløp;
    }

    public BigDecimal getDifferanse() {
        return differanse;
    }

    public BigDecimal getResultat() {
        return resultat;
    }

    public BigDecimal getFeilutbetaltBeløp() {
        return feilutbetaltBeløp;
    }

    public BigDecimal getMotregning() {
        return motregning;
    }

    public BigDecimal getEtterbetaling() {
        return etterbetaling;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SimulertBeregning kladd = new SimulertBeregning();

        public Builder medTidligereUtbetaltBeløp(BigDecimal tidligereUtbetaltBeløp) {
            kladd.tidligereUtbetaltBeløp = tidligereUtbetaltBeløp;
            return this;
        }

        public Builder medNyttBeregnetBeløp(BigDecimal nyttBeregnetBeløp) {
            kladd.nyttBeregnetBeløp = nyttBeregnetBeløp;
            return this;
        }

        public Builder medDifferanse(BigDecimal differanse) {
            kladd.differanse = differanse;
            return this;
        }

        public Builder medResultat(BigDecimal resultat) {
            kladd.resultat = resultat;
            return this;
        }

        public Builder medFeilutbetaltBeløp(BigDecimal feilutbetaling) {
            kladd.feilutbetaltBeløp = feilutbetaling;
            return this;
        }

        public Builder medMotregning(BigDecimal motregning) {
            kladd.motregning = motregning;
            return this;
        }

        public Builder medEtterbetaling(BigDecimal etterbetaling) {
            kladd.etterbetaling = etterbetaling;
            return this;
        }

        public SimulertBeregning build() {
            return kladd;
        }
    }


}
