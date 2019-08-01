package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "TilkjentYtelseBehandlingInfo")
@Table(name = "TY_BEHANDLING_INFO")
public class TilkjentYtelseBehandlingInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TY_BEHANDLING_INFO")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "aktoer_id", nullable = false)
    private String aktørId;

    @Column(name = "saksnummer", nullable = false)
    private String saksnummer;

    @Column(name = "forrige_behandling_id")
    private Long forrigeBehandlingId;

    @Column(name = "ytelse_type", nullable = false)
    private String ytelseType;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "gjelder_adopsjon", nullable = false)
    private Boolean gjelderAdopsjon;

    @Column(name = "vedtaksdato", nullable = false)
    private LocalDate vedtaksdato;

    @Column(name = "ansvarlig_saksbehandler", nullable = false)
    private String ansvarligSaksbehandler;

    @OneToOne(optional = false)
    @JoinColumn(name = "tilkjent_ytelse_id", nullable = false, updatable = false)
    private TilkjentYtelseEntitet tilkjentYtelse;


    public long getId() {
        return id;
    }

    public String getAktørId() {
        return aktørId;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public Long getForrigeBehandlingId() {
        return forrigeBehandlingId;
    }

    public String getYtelseType() {
        return ytelseType;
    }

    public boolean getGjelderAdopsjon() {
        return gjelderAdopsjon;
    }

    public LocalDate getVedtaksdato() {
        return vedtaksdato;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public TilkjentYtelseEntitet getTilkjentYtelse() {
        return tilkjentYtelse;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(TilkjentYtelseBehandlingInfo eksisterende) {
        return new Builder(eksisterende);
    }

    public static class Builder {
        private TilkjentYtelseBehandlingInfo tilkjentYtelseBehandlingInfoMal;

        public Builder() {
            this.tilkjentYtelseBehandlingInfoMal = new TilkjentYtelseBehandlingInfo();
        }

        public Builder(TilkjentYtelseBehandlingInfo eksisterende) {
            this.tilkjentYtelseBehandlingInfoMal = eksisterende;
        }

        public Builder medAktørId(String aktørId) {
            tilkjentYtelseBehandlingInfoMal.aktørId = aktørId;
            return this;
        }

        public Builder medSaksnummer(String saksnummer) {
            tilkjentYtelseBehandlingInfoMal.saksnummer = saksnummer;
            return this;
        }

        public Builder medForrigeBehandlingId(Long forrigeBehandlingId) {
            tilkjentYtelseBehandlingInfoMal.forrigeBehandlingId = forrigeBehandlingId;
            return this;
        }

        public Builder medYtelseType(String ytelseType) {
            tilkjentYtelseBehandlingInfoMal.ytelseType = ytelseType;
            return this;
        }

        public Builder medGjelderAdopsjon(boolean gjelderAdopsjon) {
            tilkjentYtelseBehandlingInfoMal.gjelderAdopsjon = gjelderAdopsjon;
            return this;
        }

        public Builder medVedtaksdato(LocalDate vedtaksdato) {
            tilkjentYtelseBehandlingInfoMal.vedtaksdato = vedtaksdato;
            return this;
        }

        public Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            tilkjentYtelseBehandlingInfoMal.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public TilkjentYtelseBehandlingInfo build(TilkjentYtelseEntitet tilkjentYtelse) {
            tilkjentYtelseBehandlingInfoMal.tilkjentYtelse = tilkjentYtelse;
            verifyStateForBuild();
            tilkjentYtelseBehandlingInfoMal.tilkjentYtelse.setTilkjentYtelseBehandlingInfo(tilkjentYtelseBehandlingInfoMal);
            return tilkjentYtelseBehandlingInfoMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(tilkjentYtelseBehandlingInfoMal.tilkjentYtelse, "tilkjentYtelse");
            Objects.requireNonNull(tilkjentYtelseBehandlingInfoMal.aktørId, "aktørId");
            Objects.requireNonNull(tilkjentYtelseBehandlingInfoMal.saksnummer, "saksnummer");
            Objects.requireNonNull(tilkjentYtelseBehandlingInfoMal.ytelseType, "ytelseType");
            Objects.requireNonNull(tilkjentYtelseBehandlingInfoMal.gjelderAdopsjon, "gjelderAdopsjon");
            Objects.requireNonNull(tilkjentYtelseBehandlingInfoMal.ansvarligSaksbehandler, "ansvarligSaksbehandler");
        }
    }
}
