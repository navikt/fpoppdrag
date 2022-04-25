package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.oppdrag.kodeverdi.MottakerType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.BaseCreateableEntitet;

@Entity(name = "SimuleringMottaker")
@Table(name = "SIMULERING_MOTTAKER")
public class SimuleringMottaker extends BaseCreateableEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIMULERING_MOTTAKER")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulering_id", nullable = false)
    private SimuleringResultat simuleringResultat;

    @Column(name = "mottaker_nummer", nullable = false)
    private String mottakerNummer;

    @Enumerated(EnumType.STRING)
    @Column(name = "mottaker_type", nullable = false)
    private MottakerType mottakerType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "simuleringMottaker")
    private List<SimulertPostering> simulertePosteringer = new ArrayList<>();

    private SimuleringMottaker() {
        // Hibernate
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getMottakerNummer() {
        return mottakerNummer;
    }

    public MottakerType getMottakerType() {
        return mottakerType;
    }

    public SimuleringResultat getSimuleringResultat() {
        return simuleringResultat;
    }

    void setSimuleringResultat(SimuleringResultat simuleringResultat) {
        this.simuleringResultat = simuleringResultat;
    }

    public List<SimulertPostering> getSimulertePosteringer() {
        return simulertePosteringer.stream().filter(sp -> !sp.erUtenInntrekk()).collect(Collectors.toList());
    }

    public List<SimulertPostering> getSimulertePosteringerUtenInntrekk() {
        return simulertePosteringer.stream().filter(SimulertPostering::erUtenInntrekk).collect(Collectors.toList());
    }

    public List<SimulertPostering> getSimulertePosteringerForFeilutbetaling() {
        // Skal ikke kunne kombinere tilbakekreving og inntrekk, bruker derfor uten inntrekk hvis det finnes
        List<SimulertPostering> simulertePosteringerUtenInntrekk = getSimulertePosteringerUtenInntrekk();
        return simulertePosteringerUtenInntrekk.isEmpty() ? getSimulertePosteringer() : simulertePosteringerUtenInntrekk;
    }

    List<SimulertPostering> getAlleSimulertePosteringer() {
        return simulertePosteringer;
    }

    public void leggTilSimulertPostering(SimulertPostering simulertPostering) {
        simulertPostering.setSimuleringMottaker(this);
        simulertePosteringer.add(simulertPostering);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimuleringMottaker that = (SimuleringMottaker) o;
        return Objects.equals(mottakerNummer, that.mottakerNummer) &&
                Objects.equals(mottakerType, that.mottakerType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mottakerNummer, mottakerType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id //$NON-NLS-1$
                + ", simuleringResultat=" + simuleringResultat.getId() //$NON-NLS-1$
                + ", mottakerNummer=" + mottakerNummer //$NON-NLS-1$
                + ", mottakerType=" + mottakerType //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public static class Builder {
        private SimuleringMottaker kladd = new SimuleringMottaker();

        public Builder medMottakerNummer(String mottakerNummer) {
            kladd.mottakerNummer = mottakerNummer;
            return this;
        }

        public Builder medMottakerType(MottakerType mottakerType) {
            kladd.mottakerType = mottakerType;
            return this;
        }

        public Builder medSimulertPostering(SimulertPostering simulertPostering) {
            Objects.requireNonNull(simulertPostering, "simulertPostering"); //NOSONAR
            simulertPostering.setSimuleringMottaker(kladd);
            kladd.simulertePosteringer.add(simulertPostering);
            return this;
        }

        public SimuleringMottaker build() {
            verify();
            return kladd;
        }

        private void verify() {
            Objects.requireNonNull(kladd.mottakerNummer, "mottakerNummer");
            Objects.requireNonNull(kladd.mottakerType, "mottakerType");
        }
    }

}
