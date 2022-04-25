package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.oppdrag.kodeverdi.BetalingType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.BaseCreateableEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "SimulertPostering")
@Table(name = "SIMULERT_POSTERING")
public class SimulertPostering extends BaseCreateableEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIMULERT_POSTERING")
    private Long id;

    @Convert(converter = FagOmrådeKode.KodeverdiConverter.class)
    @Column(name = "fag_omraade_kode", nullable = false)
    private FagOmrådeKode fagOmrådeKode = FagOmrådeKode.UDEFINERT;

    @Column(name = "fom", nullable = false)
    private LocalDate fom;

    @Column(name = "tom", nullable = false)
    private LocalDate tom;

    @Enumerated(EnumType.STRING)
    @Column(name = "betaling_type", nullable = false)
    private BetalingType betalingType;

    @Column(name = "beloep", nullable = false)
    private BigDecimal beløp;

    @Enumerated(EnumType.STRING)
    @Column(name = "postering_type")
    private PosteringType posteringType = null;

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulering_mottaker_id", nullable = false)
    private SimuleringMottaker simuleringMottaker;

    @Column(name = "forfall", nullable = false)
    private LocalDate forfallsdato;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "uten_inntrekk", nullable = false)
    private boolean utenInntrekk;

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
                + (fagOmrådeKode != null ? ", fagOmrådeKode=" + fagOmrådeKode.getKode() : "")//$NON-NLS-1$
                + ", fom=" + fom //$NON-NLS-1$
                + ", tom=" + tom //$NON-NLS-1$
                + ", betalingType=" + betalingType //$NON-NLS-1$
                + ", beløp=" + beløp //$NON-NLS-1$
                + (posteringType != null ? ", posteringType=" + posteringType : "") //$NON-NLS-1$
                + ", utenInntrekk=" + utenInntrekk //$NON-NLS-1$
                + ">"; //$NON-NLS-1$

    }

    public static class Builder {
        private FagOmrådeKode fagOmrådeKode;
        private LocalDate fom;
        private LocalDate tom;
        private BetalingType betalingType;
        private BigDecimal beløp;
        private PosteringType posteringType;
        private LocalDate forfallsdato;
        private boolean utenInntrekk;

        public Builder medFagOmraadeKode(FagOmrådeKode fagOmrådeKode) {
            this.fagOmrådeKode = fagOmrådeKode;
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

        public Builder medForfallsdato(LocalDate forfallsdato) {
            this.forfallsdato = forfallsdato;
            return this;
        }

        public Builder utenInntrekk(boolean utenInntrekk) {
            this.utenInntrekk = utenInntrekk;
            return this;
        }

        public SimulertPostering build() {
            SimulertPostering simulertPostering = new SimulertPostering();
            simulertPostering.fagOmrådeKode = fagOmrådeKode;
            simulertPostering.fom = fom;
            simulertPostering.tom = tom;
            simulertPostering.betalingType = betalingType;
            simulertPostering.beløp = beløp;
            simulertPostering.posteringType = posteringType;
            simulertPostering.forfallsdato = forfallsdato;
            simulertPostering.utenInntrekk = utenInntrekk;
            return simulertPostering;
        }
    }
}
