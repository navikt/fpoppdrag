package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@ApplicationScoped
public class PipRepository {

    private EntityManager entityManager;

    public PipRepository() {
        //CDI proxy
    }

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<String> getAktørIdForBehandling(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); //NOSONAR

        String sql = "SELECT aktoer_id from GR_SIMULERING where behandling_id = :behandlingId and aktiv = 'J'";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("behandlingId", behandlingId);

        @SuppressWarnings("rawtypes")
        List resultater = query.getResultList();
        if (resultater.isEmpty()) {
            return Optional.empty();
        } else if (resultater.size() == 1) {
            return Optional.of((String) resultater.get(0));
        } else {
            throw new IllegalStateException("Forventet 0 eller 1 treff etter søk på behandlingId, fikk flere for behandlingId " + behandlingId);
        }
    }
}