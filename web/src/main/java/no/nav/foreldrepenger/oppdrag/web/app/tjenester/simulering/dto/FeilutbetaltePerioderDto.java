package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import java.util.List;
import java.util.Objects;

public record FeilutbetaltePerioderDto(Long sumFeilutbetaling, List<PeriodeDto> perioder) {

    public static FeilutbetaltePerioderDto lagDto(Long sumFeilutbetaling, List<PeriodeDto> perioder) {
        Objects.requireNonNull(sumFeilutbetaling, "sumFeilutbetaling");
        return new FeilutbetaltePerioderDto(Math.abs(sumFeilutbetaling), perioder);
    }

}
