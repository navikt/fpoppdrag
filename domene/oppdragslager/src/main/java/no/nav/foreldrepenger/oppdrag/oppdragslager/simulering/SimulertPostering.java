package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.oppdrag.kodeverk.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverk.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverk.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverk.SatsType;
import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "SimulertPostering")
@Table(name = "SIMULERT_POSTERING")
public class SimulertPostering extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIMULERT_POSTERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "fag_omraade_kode", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + FagOmrådeKode.DISCRIMINATOR + "'"))
    private FagOmrådeKode fagOmrådeKode;

    @Column(name = "konto", nullable = false)
    private String konto;

    @Column(name = "fom", nullable = false)
    private LocalDate fom;

    @Column(name = "tom", nullable = false)
    private LocalDate tom;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "betaling_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BetalingType.DISCRIMINATOR + "'"))
    private BetalingType betalingType;

    @Column(name = "beloep", nullable = false)
    private BigDecimal beløp;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "postering_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + PosteringType.DISCRIMINATOR + "'"))
    private PosteringType posteringType;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "klasse_kode", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KlasseKode.DISCRIMINATOR + "'"))
    private KlasseKode klasseKode;


    @ManyToOne(optional = false)
    @JoinColumn(name = "simulering_mottaker_id", nullable = false)
    private SimuleringMottaker simuleringMottaker;

    @Column(name = "forfall", nullable = false)
    private LocalDate forfallsdato;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "uten_inntrekk", nullable = false)
    private boolean utenInntrekk;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "sats_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + SatsType.DISCRIMINATOR + "'"))
    private SatsType satsType = SatsType.UDEFINERT;

    @Column(name = "sats")
    private BigDecimal sats;

    private SimulertPostering() {
        // Hibernate
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public FagOmrådeKode getFagOmrådeKode() {
        return fagOmrådeKode;
    }

    public String getKonto() {
        return konto;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public BetalingType getBetalingType() {
        return betalingType;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public PosteringType getPosteringType() {
        return posteringType;
    }

    public KlasseKode getKlasseKode() {
        return klasseKode;
    }

    public SimuleringMottaker getSimuleringMottaker() {
        return simuleringMottaker;
    }

    void setSimuleringMottaker(SimuleringMottaker simuleringMottaker) {
        this.simuleringMottaker = simuleringMottaker;
    }

    public LocalDate getForfallsdato() {
        return forfallsdato;
    }

    public boolean erUtenInntrekk() {
        return utenInntrekk;
    }

    public boolean harSats() {
        return sats != null && sats.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
                + ", fagOmrådeKode=" + fagOmrådeKode //$NON-NLS-1$
                + ", konto=" + konto //$NON-NLS-1$
                + ", fom=" + fom //$NON-NLS-1$
                + ", tom=" + tom //$NON-NLS-1$
                + ", betalingType=" + betalingType //$NON-NLS-1$
                + ", beløp=" + beløp //$NON-NLS-1$
                + ", posteringType=" + posteringType //$NON-NLS-1$
                + ", klasseKode=" + klasseKode //$NON-NLS-1$
                + ", forfallsdato=" + forfallsdato //$NON-NLS-1$
                + ", utenInntrekk=" + utenInntrekk //$NON-NLS-1$
                + ">"; //$NON-NLS-1$

    }

    public static class Builder {
        private String konto;
        private FagOmrådeKode fagOmrådeKode;
        private LocalDate fom;
        private LocalDate tom;
        private BetalingType betalingType;
        private BigDecimal beløp;
        private PosteringType posteringType;
        private KlasseKode klasseKode;
        private LocalDate forfallsdato;
        private boolean utenInntrekk;
        private SatsType satsType = SatsType.UDEFINERT;
        private BigDecimal sats;

        public Builder medFagOmraadeKode(FagOmrådeKode fagOmrådeKode) {
            this.fagOmrådeKode = fagOmrådeKode;
            return this;
        }

        public Builder medKonto(String konto) {
            this.konto = konto;
            return this;
        }

        public Builder medFom(LocalDate fom) {
            this.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            this.tom = tom;
            return this;
        }

        public Builder medBetalingType(BetalingType betalingType) {
            this.betalingType = betalingType;
            return this;
        }

        public Builder medBeløp(BigDecimal beløp) {
            this.beløp = beløp.abs(); // Lagrer positive beløp, BetalingType angir om det skal tolkes negativt eller positivt
            return this;
        }

        public Builder medPosteringType(PosteringType posteringType) {
            this.posteringType = posteringType;
            return this;
        }

        public Builder medKlasseKode(KlasseKode klasseKode) {
            this.klasseKode = klasseKode;
            return this;
        }

        public Builder medForfallsdato(LocalDate forfallsdato) {
            this.forfallsdato = forfallsdato;
            return this;
        }

        public Builder utenInntrekk(boolean utenInntrekk) {
            this.utenInntrekk = utenInntrekk;
            return this;
        }

        public Builder medSatsType(SatsType satsType) {
            this.satsType = satsType;
            return this;
        }

        public Builder medSats(BigDecimal sats) {
            this.sats = sats;
            return this;
        }

        public SimulertPostering build() {
            SimulertPostering simulertPostering = new SimulertPostering();
            simulertPostering.fagOmrådeKode = fagOmrådeKode;
            simulertPostering.konto = konto;
            simulertPostering.fom = fom;
            simulertPostering.tom = tom;
            simulertPostering.betalingType = betalingType;
            simulertPostering.beløp = beløp;
            simulertPostering.posteringType = posteringType;
            simulertPostering.klasseKode = klasseKode;
            simulertPostering.forfallsdato = forfallsdato;
            simulertPostering.utenInntrekk = utenInntrekk;
            simulertPostering.satsType = satsType;
            simulertPostering.sats = sats;
            return simulertPostering;
        }
    }
}
