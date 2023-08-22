package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

public class SimuleringDto implements AbacDto {
    @NotNull
    @Min(1)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @NotNull
    @Pattern(regexp = "^\\d*$")
    @Size(min = 1, max = 50)
    private String aktørId;

    @Valid
    @Size(min = 1)
    private List<SimuleringMottakerDto> simuleringMottakerListe;

    private SimuleringDto() {
        // CDI
    }

    public SimuleringDto(Long behandlingId, String aktørId, List<SimuleringMottakerDto> simuleringMottakerListe) {
        this.behandlingId = behandlingId;
        this.aktørId = aktørId;
        this.simuleringMottakerListe = simuleringMottakerListe;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public String getAktørId() {
        return aktørId;
    }

    public List<SimuleringMottakerDto> getSimuleringMottakerListe() {
        return simuleringMottakerListe;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, behandlingId)
                .leggTil(StandardAbacAttributtType.AKTØR_ID, aktørId);
    }
}
