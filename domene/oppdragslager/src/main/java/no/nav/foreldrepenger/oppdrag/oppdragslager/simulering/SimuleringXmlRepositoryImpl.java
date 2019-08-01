package no.nav.foreldrepenger.oppdrag.oppdragslager.simulering;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * @deprecated lagring av request XML og response XML skal fjernes i fremtiden
 * @since 30.11.2018
 */
@Deprecated
@ApplicationScoped
public class SimuleringXmlRepositoryImpl implements SimuleringXmlRepository {

    private EntityManager entityManager;

    SimuleringXmlRepositoryImpl() {
        // CDI
    }

    @Inject
    public SimuleringXmlRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public void lagre(SimuleringXml simuleringXml) {
        Optional<SimuleringXml> eksisterendeOpt = hentSimuleringXml(simuleringXml.getEksternReferanse().getBehandlingId()).stream()
                .filter(obj -> obj.equals(simuleringXml))
                .findFirst();

        if (eksisterendeOpt.isPresent()) {
            SimuleringXml eksisterende = eksisterendeOpt.get();
            eksisterende.setResponseXml(simuleringXml.getResponseXml());
            entityManager.persist(eksisterende);

        } else {
            entityManager.persist(simuleringXml);
        }
        entityManager.flush();
    }

    @Override
    public void nyTransaksjon() {
        entityManager.flush();
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
    }

    @Override
    public List<SimuleringXml> hentSimuleringXml(Long behandlingId) {
        return entityManager.createQuery(
                "from SimuleringXml s" +
                        " where s.eksternReferanse.behandlingId = :behandlingId",
                SimuleringXml.class)
                .setParameter("behandlingId", behandlingId)
                .getResultList();
    }
}
