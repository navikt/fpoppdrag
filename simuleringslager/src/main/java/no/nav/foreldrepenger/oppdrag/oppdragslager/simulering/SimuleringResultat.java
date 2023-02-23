package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.oppdrag.oppdragslager.BaseCreateableEntitet;


@Entity(name = "SimuleringResultat")
@Table(name = "SIMULERING_RESULTAT")
public class SimuleringResultat extends BaseCreateableEntitet {

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
        return "SimuleringResultat{" +
                "simuleringMottakere=" + simuleringMottakere +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimuleringResultat that = (SimuleringResultat) o;
        return erSetteneLike(simuleringMottakere, that.simuleringMottakere);
    }

    private boolean erSetteneLike(Set<?> l1, Set<?> l2) {
        var cp = new ArrayList<>( l1 );
        for ( Object o : l2 ) {
            if ( !cp.remove( o ) ) {
                return false;
            }
        }
        return cp.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(simuleringMottakere);
    }
}
