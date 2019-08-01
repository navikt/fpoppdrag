package no.nav.foreldrepenger.oppdrag.iverksett.hent.tilkjentytelse;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.util.InputValideringRegex;

public class TilkjentYtelseMelding {

    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long fagsakId;

    @Min(0)
    @Max(Long.MAX_VALUE)
    private long behandlingId;

    @NotNull
    @Digits(integer = 13, fraction = 0)
    @JsonProperty("aktoerId")
    private String aktørId;

    @NotNull
    @JsonProperty("ivSystem")
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    @Size(min = 1, max = 100)
    private String iverksettingSystem = "fpsak";

    public Long getFagsakId() {
        return fagsakId;
    }

    public TilkjentYtelseMelding setFagsakId(Long fagsakId) {
        this.fagsakId = fagsakId;
        return this;
    }

    public long getBehandlingId() {
        return behandlingId;
    }

    public TilkjentYtelseMelding setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
        return this;
    }

    public String getAktørId() {
        return aktørId;
    }

    public TilkjentYtelseMelding setAktørId(String aktørId) {
        this.aktørId = aktørId;
        return this;
    }

    public String getIverksettingSystem() {
        return iverksettingSystem;
    }

    public TilkjentYtelseMelding setIverksettingSystem(String iverksettingSystem) {
        this.iverksettingSystem = iverksettingSystem;
        return this;
    }
}
