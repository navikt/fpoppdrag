package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BehandlingIdDto(@NotNull @Min(0) @Max(Long.MAX_VALUE) Long behandlingId) { }
