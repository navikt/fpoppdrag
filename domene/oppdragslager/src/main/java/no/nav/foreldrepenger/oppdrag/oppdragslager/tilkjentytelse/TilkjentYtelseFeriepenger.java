package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "TilkjentYtelseFeriepenger")
@Table(name = "TY_FERIEPENGER")
public class TilkjentYtelseFeriepenger extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TY_FERIEPENGER")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "opptjeningsaar", nullable = false)
    private int opptjeningsår;

    @Column(name = "aarsbeloep", nullable = false)
    private long årsbeløp;

    @OneToOne(optional = false)
    @JoinColumn(name = "ty_andel_id", nullable = false, updatable = false)
    private TilkjentYtelseAndel tilkjentYtelseAndel;


    public int getOpptjeningsår() {
        return opptjeningsår;
    }

    public long getÅrsbeløp() {
        return årsbeløp;
    }

    public TilkjentYtelseAndel getTilkjentYtelseAndel() {
        return tilkjentYtelseAndel;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof TilkjentYtelseFeriepenger)) {
            return false;
        }
        TilkjentYtelseFeriepenger other = (TilkjentYtelseFeriepenger) object;
        return Objects.equals(this.getOpptjeningsår(), other.getOpptjeningsår())
                && Objects.equals(this.getÅrsbeløp(), other.getÅrsbeløp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(opptjeningsår, årsbeløp);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TilkjentYtelseFeriepenger tilkjentYtelseFeriepengerMal;

        public Builder() {
            this.tilkjentYtelseFeriepengerMal = new TilkjentYtelseFeriepenger();
        }

        public Builder medOpptjeningsår(int opptjeningsår) {
            tilkjentYtelseFeriepengerMal.opptjeningsår = opptjeningsår;
            return this;
        }

        public Builder medÅrsbeløp(long årsbeløp) {
            tilkjentYtelseFeriepengerMal.årsbeløp = årsbeløp;
            return this;
        }

        public TilkjentYtelseFeriepenger build(TilkjentYtelseAndel tilkjentYtelseAndel) {
            tilkjentYtelseFeriepengerMal.tilkjentYtelseAndel = tilkjentYtelseAndel;
            verifyStateForBuild();
            tilkjentYtelseFeriepengerMal.tilkjentYtelseAndel.leggTilFeriepenger(tilkjentYtelseFeriepengerMal);
            return tilkjentYtelseFeriepengerMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(tilkjentYtelseFeriepengerMal.tilkjentYtelseAndel, "tilkjentYtelseAndel");
        }
    }
}
