package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Entity(name = "SimuleringGrunnlag")
@Table(name = "GR_SIMULERING")
public class SimuleringGrunnlag extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_SIMULERING")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    @Embedded
    private BehandlingRef eksternReferanse;

    @Column(name = "aktoer_id", nullable = false)
    private String aktørId;

    @OneToOne(optional = false)
    @JoinColumn(name = "simulering_id", nullable = false)
    private SimuleringResultat simuleringResultat;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv;

    @Column(name = "simulering_kjoert_dato", nullable = false, updatable = false)
    private LocalDateTime simuleringKjørtDato = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseType ytelseType;

    private SimuleringGrunnlag() {
        // Hibernate
    }

    public LocalDateTime getSimuleringKjørtDato() {
        return simuleringKjørtDato;
    }

    public Long getId() {
        return id;
    }

    public BehandlingRef getEksternReferanse() {
        return eksternReferanse;
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public SimuleringResultat getSimuleringResultat() {
        return simuleringResultat;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SimuleringGrunnlag kladd = new SimuleringGrunnlag();

        public Builder medEksternReferanse(BehandlingRef eksternReferanse) {
            kladd.eksternReferanse = eksternReferanse;
            return this;
        }

        public Builder medAktørId(String aktørId) {
            kladd.aktørId = aktørId;
            return this;
        }

        public Builder medSimuleringResultat(SimuleringResultat simuleringResultat) {
            kladd.simuleringResultat = simuleringResultat;
            return this;
        }

        public Builder medSimuleringKjørtDato(LocalDateTime simuleringKjørtDato) {
            kladd.simuleringKjørtDato = simuleringKjørtDato;
            return this;
        }

        public Builder medYtelseType(YtelseType ytelseType) {
            kladd.ytelseType = ytelseType;
            return this;
        }

        public SimuleringGrunnlag build() {
            valider();
            return kladd;
        }

        private void valider() {
            Objects.requireNonNull(kladd.eksternReferanse, "Utvikler-feil: Må ha med behandlingId");
            Objects.requireNonNull(kladd.aktørId, "Utvikler-feil: Må ha med aktørId");
            Objects.requireNonNull(kladd.simuleringResultat, "Utvikler-feil: Må ha med resulat");
            Objects.requireNonNull(kladd.ytelseType, "Utvikler-feil: Må ha med ytelseType");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
                + ", simuleringResultat=" + simuleringResultat //$NON-NLS-1$
                + ", eksternReferanse=" + eksternReferanse.getBehandlingId() //$NON-NLS-1$
                + ", aktiv=" + aktiv //$NON-NLS-1$
                + ", ytelseType=" + ytelseType //$NON-NLS-1$
                + ", versjon=" + versjon //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimuleringGrunnlag that = (SimuleringGrunnlag) o;
        return Objects.equals(eksternReferanse, that.eksternReferanse) &&
                Objects.equals(aktørId, that.aktørId) &&
                Objects.equals(simuleringResultat, that.simuleringResultat) &&
                ytelseType == that.ytelseType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eksternReferanse, aktørId, simuleringResultat, ytelseType);
    }
}
