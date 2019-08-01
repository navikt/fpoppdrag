package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.test.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

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
        // resteasy
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
                .leggTilAktørId(aktørId);
    }
}
