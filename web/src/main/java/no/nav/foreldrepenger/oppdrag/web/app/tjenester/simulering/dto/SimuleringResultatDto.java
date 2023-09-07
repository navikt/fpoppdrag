package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.math.BigDecimal;

public record SimuleringResultatDto(Long sumFeilutbetaling, Long sumInntrekk, boolean slåttAvInntrekk) {

    public SimuleringResultatDto(BigDecimal sumFeilutbetaling, BigDecimal sumInntrekk, boolean slåttAvInntrekk) {
        this(sumFeilutbetaling != null ? sumFeilutbetaling.longValue() : null,
            sumInntrekk != null ? sumInntrekk.longValue() : null,
            slåttAvInntrekk);
    }

}
