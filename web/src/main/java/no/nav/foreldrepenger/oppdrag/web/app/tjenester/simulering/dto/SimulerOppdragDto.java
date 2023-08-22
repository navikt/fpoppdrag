package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.util.InputValideringRegex;

public class SimulerOppdragDto implements AbacDto {
    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @NotNull
    @Valid
    @Size(min = 1, max = 100)
    private List<String> oppdragPrMottaker;

    @Pattern(regexp = InputValideringRegex.KODEVERK)
    @Size(max = 20)
    private String behandlingÅrsakKode;

    public SimulerOppdragDto() {
        //
    }

    public SimulerOppdragDto(Long behandlingId, List<String> oppdragPrMottaker) {
        this.behandlingId = behandlingId;
        this.oppdragPrMottaker = oppdragPrMottaker;
    }

    public SimulerOppdragDto(Long behandlingId, List<String> oppdragPrMottaker, String behandlingÅrsakKode) {
        this(behandlingId, oppdragPrMottaker);
        this.behandlingÅrsakKode = behandlingÅrsakKode;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public List<String> getOppdragPrMottaker() {
        return oppdragPrMottaker;
    }

    public String getBehandlingÅrsakKode() {
        return behandlingÅrsakKode;
    }

    @JsonIgnore
    public List<String> getOppdragPrMottakerDecoded() {
        return oppdragPrMottaker.stream()
                .filter(Objects::nonNull)
                .map(str -> new String(Base64.getDecoder().decode(str.getBytes(UTF_8)), UTF_8))
                .toList();
    }

    @JsonIgnore
    public static SimulerOppdragDto lagDto(Long behandlingId, List<String> råXml) {
        Objects.requireNonNull(råXml, "Rå XML kan ikke være null");
        var encoded = råXml.stream()
                .map(str -> Base64.getEncoder()
                        .encodeToString(str.getBytes(UTF_8)))
                .toList();
        return new SimulerOppdragDto(behandlingId, encoded);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, behandlingId);
    }
}
