package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.oppdrag.oppdragslager.BaseEntitet;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.tid.DatoIntervallEntitet;

@Entity(name = "TilkjentYtelsePeriode")
@Table(name = "TILKJENT_YTELSE_PERIODE")
public class TilkjentYtelsePeriode extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TILKJENT_YTELSE_PERIODE")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "tom")),
    })
    private DatoIntervallEntitet periode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tilkjentYtelsePeriode")
    private List<TilkjentYtelseAndel> tilkjentYtelseAndelListe = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "tilkjent_ytelse_id", nullable = false, updatable = false)
    private TilkjentYtelseEntitet tilkjentYtelse;

    public TilkjentYtelsePeriode() {
        // default constructor
    }

    public Long getId() {
        return id;
    }

    public LocalDate getTilkjentYtelsePeriodeFom() {
        return periode.getFomDato();
    }

    public LocalDate getTilkjentYtelsePeriodeTom() {
        return periode.getTomDato();
    }

    public List<TilkjentYtelseAndel> getTilkjentYtelseAndelListe() {
        return tilkjentYtelseAndelListe;
    }

    public void leggTilTilkjentYtelseAndel(TilkjentYtelseAndel tilkjentYtelseAndel) {
        Objects.requireNonNull(tilkjentYtelseAndel, "tilkjentYtelseAndel");
        if (!tilkjentYtelseAndelListe.contains(tilkjentYtelseAndel)) {
            tilkjentYtelseAndelListe.add(tilkjentYtelseAndel);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof TilkjentYtelsePeriode)) {
            return false;
        }
        TilkjentYtelsePeriode other = (TilkjentYtelsePeriode) object;
        return Objects.equals(this.getTilkjentYtelsePeriodeFom(), other.getTilkjentYtelsePeriodeFom())
                && Objects.equals(this.getTilkjentYtelsePeriodeTom(), other.getTilkjentYtelsePeriodeTom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(TilkjentYtelsePeriode eksisterende) {
        return new Builder(eksisterende);
    }

    public static class Builder {
        private TilkjentYtelsePeriode tilkjentYtelsePeriodeMal;

        public Builder() {
            this.tilkjentYtelsePeriodeMal = new TilkjentYtelsePeriode();
        }

        public Builder(TilkjentYtelsePeriode eksisterende) {
            this.tilkjentYtelsePeriodeMal = eksisterende;
        }

        public Builder medTilkjentYtelsePeriodeFomOgTom(LocalDate periodeFom, LocalDate periodeTom) {
            tilkjentYtelsePeriodeMal.periode = DatoIntervallEntitet.fraOgMedTilOgMed(periodeFom, periodeTom);
            return this;
        }

        public TilkjentYtelsePeriode build(TilkjentYtelseEntitet tilkjentYtelse) {
            tilkjentYtelsePeriodeMal.tilkjentYtelse = tilkjentYtelse;
            verifyStateForBuild();
            tilkjentYtelsePeriodeMal.tilkjentYtelse.leggTilTilkjentYtelsePeriode(tilkjentYtelsePeriodeMal);
            return tilkjentYtelsePeriodeMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(tilkjentYtelsePeriodeMal.tilkjentYtelse, "tilkjentYtelse");
            Objects.requireNonNull(tilkjentYtelsePeriodeMal.periode, "tilkjentYtelsePeriode");
            Objects.requireNonNull(tilkjentYtelsePeriodeMal.periode.getFomDato(), "tilkjentYtelsePeriodeFom");
            Objects.requireNonNull(tilkjentYtelsePeriodeMal.periode.getTomDato(), "tilkjentYtelsePeriodeTom");
        }
    }
}
