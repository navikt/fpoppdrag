package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BehandlingIdDto(@NotNull @Min(0) @Max(Long.MAX_VALUE) Long behandlingId,
                              @Valid UUID behandlingUuid,
                              @Digits(integer = 18, fraction = 0) String saksnummer) { }
