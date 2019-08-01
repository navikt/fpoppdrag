package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "TilkjentYtelseEntitet")
@Table(name = "TILKJENT_YTELSE")
public class TilkjentYtelseEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TILKJENT_YTELSE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "behandling_id", nullable = false)
    private Long behandlingId;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "er_opphoer", nullable = false)
    private Boolean erOpphør;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "er_opphoer_etter_stp")
    private Boolean erOpphørEtterSkjæringstidspunktet;

    @Column(name = "endringsdato")
    private LocalDate endringsdato;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "tilkjentYtelse")
    private TilkjentYtelseBehandlingInfo tilkjentYtelseBehandlingInfo;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tilkjentYtelse")
    private List<TilkjentYtelsePeriode> tilkjentYtelsePeriodeListe = new ArrayList<>();

    public long getId() {
        return id;
    }

    public long getBehandlingId() {
        return behandlingId;
    }

    public boolean getErOpphør() {
        return erOpphør;
    }

    public Boolean getErOpphørEtterSkjæringstidspunktet() {
        return erOpphørEtterSkjæringstidspunktet;
    }

    public Optional<LocalDate> getEndringsdato() {
        return Optional.ofNullable(endringsdato);
    }

    public List<TilkjentYtelsePeriode> getTilkjentYtelsePeriodeListe() {
        return tilkjentYtelsePeriodeListe;
    }

    public TilkjentYtelseBehandlingInfo getTilkjentYtelseBehandlingInfo() {
        return tilkjentYtelseBehandlingInfo;
    }

    public void setTilkjentYtelseBehandlingInfo(TilkjentYtelseBehandlingInfo tilkjentYtelseBehandlingInfo) {
        Objects.requireNonNull(tilkjentYtelseBehandlingInfo, "tilkjentYtelseBehandlingInfo");
        this.tilkjentYtelseBehandlingInfo = tilkjentYtelseBehandlingInfo;
    }

    public void leggTilTilkjentYtelsePeriode(TilkjentYtelsePeriode tilkjentYtelsePeriode) {
        Objects.requireNonNull(tilkjentYtelsePeriode, "tilkjentYtelsePeriode");
        if (!tilkjentYtelsePeriodeListe.contains(tilkjentYtelsePeriode)) {
            tilkjentYtelsePeriodeListe.add(tilkjentYtelsePeriode);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(TilkjentYtelseEntitet eksisterende) {
        return new Builder(eksisterende);
    }

    public static class Builder {
        private TilkjentYtelseEntitet tilkjentYtelseMal;

        public Builder() {
            this.tilkjentYtelseMal = new TilkjentYtelseEntitet();
        }

        public Builder(TilkjentYtelseEntitet eksisterende) {
            this.tilkjentYtelseMal = eksisterende;
        }

        public Builder medBehandlingId(long behandlingId) {
            tilkjentYtelseMal.behandlingId = behandlingId;
            return this;
        }

        public Builder medErOpphør(boolean erOpphør) {
            tilkjentYtelseMal.erOpphør = erOpphør;
            return this;
        }

        public Builder medErOpphørEtterStp(Boolean erOpphørEtterStp) {
            tilkjentYtelseMal.erOpphørEtterSkjæringstidspunktet = erOpphørEtterStp;
            return this;
        }

        public Builder medEndringsdato(LocalDate endringsdato) {
            tilkjentYtelseMal.endringsdato = endringsdato;
            return this;
        }

        public TilkjentYtelseEntitet build() {
            verifyStateForBuild();
            return tilkjentYtelseMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(tilkjentYtelseMal.behandlingId, "behandlingId");
            Objects.requireNonNull(tilkjentYtelseMal.erOpphør, "erOpphør");
            Objects.requireNonNull(tilkjentYtelseMal.tilkjentYtelsePeriodeListe, "tilkjentYtelsePeriodeListe");
        }
    }
}
