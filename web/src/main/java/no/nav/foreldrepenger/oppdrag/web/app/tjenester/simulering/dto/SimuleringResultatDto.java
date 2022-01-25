package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.math.BigDecimal;

public class SimuleringResultatDto {

    private Long sumFeilutbetaling;
    private Long sumInntrekk;
    private boolean slåttAvInntrekk;

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public Long getSumInntrekk() {
        return sumInntrekk;
    }

    public boolean isSlåttAvInntrekk() {
        return slåttAvInntrekk;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SimuleringResultatDto kladd = new SimuleringResultatDto();

        public Builder medSumFeilutbetaling(BigDecimal sumFeilutbetaling) {
            kladd.sumFeilutbetaling = sumFeilutbetaling != null ? sumFeilutbetaling.longValue() : null;
            return this;
        }

        public Builder medSumInntrekk(BigDecimal sumInntrekk) {
            kladd.sumInntrekk = sumInntrekk != null ? sumInntrekk.longValue() : null;
            return this;
        }

        public Builder medSlåttAvInntrekk(boolean slåttAvInntrekk) {
            kladd.slåttAvInntrekk = slåttAvInntrekk;
            return this;
        }

        public SimuleringResultatDto build() {
            return kladd;
        }
    }
}
