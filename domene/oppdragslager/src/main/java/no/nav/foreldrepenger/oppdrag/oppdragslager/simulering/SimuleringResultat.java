package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.oppdrag.oppdragslager.BaseEntitet;


@Entity(name = "SimuleringResultat")
@Table(name = "SIMULERING_RESULTAT")
public class SimuleringResultat extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SIMULERING_RESULTAT")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "simuleringResultat")
    private Set<SimuleringMottaker> simuleringMottakere = new HashSet<>();

    private SimuleringResultat() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public Set<SimuleringMottaker> getSimuleringMottakere() {
        return simuleringMottakere;
    }


    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private SimuleringResultat kladd = new SimuleringResultat();

        public Builder medSimuleringMottaker(SimuleringMottaker simuleringMottaker) {
            simuleringMottaker.setSimuleringResultat(kladd);
            kladd.simuleringMottakere.add(simuleringMottaker);
            return this;
        }

        public SimuleringResultat build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + id + " >"; //$NON-NLS-1$

    }
}
