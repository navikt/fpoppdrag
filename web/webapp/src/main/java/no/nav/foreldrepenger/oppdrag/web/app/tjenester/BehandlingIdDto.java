package no.nav.foreldrepenger.oppdrag.web.app.tjenester;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class BehandlingIdDto implements AbacDto {
    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    public BehandlingIdDto() {
        behandlingId = null; // NOSONAR
    }

    public BehandlingIdDto(String behandlingId) {
        this.behandlingId = Long.valueOf(behandlingId);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abac = AbacDataAttributter.opprett();
        return abac.leggTilBehandlingsId(getBehandlingId());
    }

    public Long getBehandlingId() {
        return behandlingId;
    }
}
