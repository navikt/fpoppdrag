package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.dto;

import java.util.List;
import java.util.Objects;

public class FeilutbetaltePerioderDto {
    private Long sumFeilutbetaling;
    private List<PeriodeDto> perioder;

    public FeilutbetaltePerioderDto(Long sumFeilutbetaling, List<PeriodeDto> perioder) {
        Objects.requireNonNull(sumFeilutbetaling, "sumFeilutbetaling");
        this.sumFeilutbetaling = Math.abs(sumFeilutbetaling);
        this.perioder = perioder;
    }

    public Long getSumFeilutbetaling() {
        return sumFeilutbetaling;
    }

    public List<PeriodeDto> getPerioder() {
        return perioder;
    }
}
