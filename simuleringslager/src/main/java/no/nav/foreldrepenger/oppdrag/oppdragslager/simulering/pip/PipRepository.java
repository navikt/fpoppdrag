package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.pip;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

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

        @SuppressWarnings("unchecked")
        List<String> resultaterRaw = query.getResultList();
        Set<String> resultater = new HashSet<>(resultaterRaw);
        if (resultater.isEmpty()) {
            return Optional.empty();
        } else if (resultater.size() == 1) {
            return resultater.stream().findFirst();
        } else {
            throw new IllegalStateException("Forventet 0 eller 1 treff etter søk på behandlingId, fikk flere for behandlingId " + behandlingId);
        }
    }
}
