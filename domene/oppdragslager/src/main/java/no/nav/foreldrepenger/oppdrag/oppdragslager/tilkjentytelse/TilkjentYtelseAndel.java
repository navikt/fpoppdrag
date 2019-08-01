package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.oppdrag.kodeverk.Inntektskategori;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "TilkjentYtelseAndel")
@Table(name = "TILKJENT_YTELSE_ANDEL")
public class TilkjentYtelseAndel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TILKJENT_YTELSE_ANDEL")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "utbetales_til_bruker", nullable = false)
    private Boolean utbetalesTilBruker;

    @Column(name = "arbeidsgiver_org_nr")
    private String arbeidsgiverOrgNr;

    @Column(name = "arbeidsgiver_aktoer_id")
    private String arbeidsgiverAktørId;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "inntektskategori", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Inntektskategori.DISCRIMINATOR + "'"))
    private Inntektskategori inntektskategori;

    @Column(name = "utbetalingsgrad", nullable = false)
    private BigDecimal utbetalingsgrad;

    @Column(name = "sats_beloep", nullable = false)
    private long satsBeløp;

    @Column(name = "sats_type", nullable = false)
    private String satsType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tilkjentYtelseAndel")
    private List<TilkjentYtelseFeriepenger> tilkjentYtelseFeriepenger = new ArrayList<>(1);

    @ManyToOne(optional = false)
    @JoinColumn(name = "ty_periode_id", nullable = false, updatable = false)
    private TilkjentYtelsePeriode tilkjentYtelsePeriode;


    public long getId() {
        return id;
    }

    public boolean getUtbetalesTilBruker() {
        return utbetalesTilBruker;
    }

    public String getArbeidsgiverOrgNr() {
        return arbeidsgiverOrgNr;
    }

    public String getArbeidsgiverAktørId() {
        return arbeidsgiverAktørId;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public long getSatsBeløp() {
        return satsBeløp;
    }

    public String getSatsType() {
        return satsType;
    }

    public List<TilkjentYtelseFeriepenger> getTilkjentYtelseFeriepenger() {
        return tilkjentYtelseFeriepenger;
    }

    public TilkjentYtelsePeriode getTilkjentYtelsePeriode() {
        return tilkjentYtelsePeriode;
    }

    public void leggTilFeriepenger(TilkjentYtelseFeriepenger tilkjentYtelseFeriepenger) {
        Objects.requireNonNull(tilkjentYtelseFeriepenger, "tilkjentYtelseFeriepenger");
        this.tilkjentYtelseFeriepenger.add(tilkjentYtelseFeriepenger);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof TilkjentYtelseAndel)) {
            return false;
        }
        TilkjentYtelseAndel other = (TilkjentYtelseAndel) object;
        return Objects.equals(this.getUtbetalesTilBruker(), other.getUtbetalesTilBruker())
                && Objects.equals(this.getArbeidsgiverOrgNr(), other.getArbeidsgiverOrgNr())
                && Objects.equals(this.getArbeidsgiverAktørId(), other.getArbeidsgiverAktørId())
                && Objects.equals(this.getInntektskategori(), other.getInntektskategori())
                && Objects.equals(this.getSatsBeløp(), other.getSatsBeløp())
                && Objects.equals(this.getSatsType(), other.getSatsType())
                && Objects.equals(this.getUtbetalingsgrad(), other.getUtbetalingsgrad());
    }

    @Override
    public int hashCode() {
        return Objects.hash(utbetalesTilBruker, arbeidsgiverOrgNr, arbeidsgiverAktørId, inntektskategori, satsBeløp, satsType, utbetalingsgrad);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(TilkjentYtelseAndel eksisterende) {
        return new Builder(eksisterende);
    }

    public static class Builder {
        private TilkjentYtelseAndel tilkjentYtelseAndelMal;

        public Builder() {
            this.tilkjentYtelseAndelMal = new TilkjentYtelseAndel();
        }

        public Builder(TilkjentYtelseAndel eksisterende) {
            this.tilkjentYtelseAndelMal = eksisterende;
        }

        public Builder medUtbetalesTilBruker(boolean utbetalesTilBruker) {
            tilkjentYtelseAndelMal.utbetalesTilBruker = utbetalesTilBruker;
            return this;
        }

        public Builder medArbeidsgiverOrgNr(String arbeidsgiverOrgNr) {
            tilkjentYtelseAndelMal.arbeidsgiverOrgNr = arbeidsgiverOrgNr;
            return this;
        }

        public Builder medArbeidsgiverAktørId(String arbeidsgiverAktørId) {
            tilkjentYtelseAndelMal.arbeidsgiverAktørId = arbeidsgiverAktørId;
            return this;
        }

        public Builder medInntektskategori(Inntektskategori inntektskategori) {
            tilkjentYtelseAndelMal.inntektskategori = inntektskategori;
            return this;
        }

        public Builder medSatsBeløp(long satsBeløp) {
            tilkjentYtelseAndelMal.satsBeløp = satsBeløp;
            return this;
        }

        public Builder medSatsType(String satsType) {
            tilkjentYtelseAndelMal.satsType = satsType;
            return this;
        }

        public Builder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            tilkjentYtelseAndelMal.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public TilkjentYtelseAndel build(TilkjentYtelsePeriode tilkjentYtelsePeriode) {
            tilkjentYtelseAndelMal.tilkjentYtelsePeriode = tilkjentYtelsePeriode;
            verifyStateForBuild();
            tilkjentYtelseAndelMal.tilkjentYtelsePeriode.leggTilTilkjentYtelseAndel(tilkjentYtelseAndelMal);
            return tilkjentYtelseAndelMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(tilkjentYtelseAndelMal.tilkjentYtelsePeriode, "tilkjentYtelse");
            Objects.requireNonNull(tilkjentYtelseAndelMal.utbetalesTilBruker, "utbetalesTilBruker");
            Objects.requireNonNull(tilkjentYtelseAndelMal.inntektskategori, "inntektskategori");
            Objects.requireNonNull(tilkjentYtelseAndelMal.satsType, "satsType");
            Objects.requireNonNull(tilkjentYtelseAndelMal.utbetalingsgrad, "utbetalingsgrad");
        }
    }
}
