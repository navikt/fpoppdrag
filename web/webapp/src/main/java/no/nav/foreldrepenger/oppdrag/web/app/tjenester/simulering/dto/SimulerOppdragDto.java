package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
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
                .map(str -> new String(Base64.getDecoder().decode(str.getBytes(Charset.forName("UTF-8"))), UTF_8))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public static SimulerOppdragDto lagDto(Long behandlingId, List<String> råXml) {
        Objects.requireNonNull(råXml, "Rå XML kan ikke være null");
        List<String> encoded = råXml.stream()
                .map(str -> Base64.getEncoder()
                        .encodeToString(str.getBytes(Charset.forName("UTF-8"))))
                .collect(Collectors.toList());
        return new SimulerOppdragDto(behandlingId, encoded);
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = AbacDataAttributter.opprett();
        return abacDataAttributter.leggTilBehandlingsId(behandlingId);
    }
}