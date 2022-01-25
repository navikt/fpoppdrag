package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class SimuleringRepository {

    private EntityManager entityManager;

    SimuleringRepository() {
        // for CDI proxy
    }

    @Inject
    public SimuleringRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public void lagreSimuleringGrunnlag(SimuleringGrunnlag simuleringGrunnlag) {
        Optional<SimuleringGrunnlag> finnesForBehandling = hentSimulertOppdragForBehandling(simuleringGrunnlag.getEksternReferanse().getBehandlingId());
        if (finnesForBehandling.isPresent()) {
            SimuleringGrunnlag eksisterendeGrunnlag = finnesForBehandling.get();
            eksisterendeGrunnlag.setAktiv(false);
            entityManager.persist(eksisterendeGrunnlag);
        }
        simuleringGrunnlag.setAktiv(true);
        lagreSimuleringResultat(simuleringGrunnlag.getSimuleringResultat());
        entityManager.persist(simuleringGrunnlag);
        entityManager.flush();
    }

    private void lagreSimuleringResultat(SimuleringResultat simuleringResultat) {
        Objects.requireNonNull(simuleringResultat, "simuleringResultat");
        entityManager.persist(simuleringResultat);
        Set<SimuleringMottaker> simuleringMottakerListe = simuleringResultat.getSimuleringMottakere();
        for (SimuleringMottaker mottaker : simuleringMottakerListe) {
            entityManager.persist(mottaker);
            for (SimulertPostering postering : mottaker.getAlleSimulertePosteringer()) {
                entityManager.persist(postering);
            }
        }
    }

    public void deaktiverSimuleringGrunnlag(SimuleringGrunnlag simuleringGrunnlag) {
        Objects.requireNonNull(simuleringGrunnlag, "simuleringGrunnlag");
        simuleringGrunnlag.setAktiv(false);
        entityManager.persist(simuleringGrunnlag);
        entityManager.flush();
    }

    public Optional<SimuleringGrunnlag> hentSimulertOppdragForBehandling(Long behandlingId) {
        List<SimuleringGrunnlag> grunnlag = entityManager.createQuery(
                        "from SimuleringGrunnlag s" +
                                " where s.eksternReferanse.behandlingId = :behandlingId" +
                                " and s.aktiv = :aktiv", SimuleringGrunnlag.class)
                .setParameter("behandlingId", behandlingId)
                .setParameter("aktiv", true)
                .getResultList();

        return grunnlag.isEmpty() ? Optional.empty() : Optional.of(grunnlag.get(0));
    }

}
