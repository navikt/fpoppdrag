package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class ØkonomioppdragRepositoryImpl implements ØkonomioppdragRepository {

    private static final String SAKSNR = "saksnr";

    private EntityManager entityManager;

    ØkonomioppdragRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public ØkonomioppdragRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public Oppdragskontroll hentOppdragskontroll(long oppdragskontrollId) {
        TypedQuery<Oppdragskontroll> query = entityManager.createQuery(
                "from Oppdragskontroll where id=:oppdragskontrollId", Oppdragskontroll.class); //$NON-NLS-1$
        query.setParameter("oppdragskontrollId", oppdragskontrollId); //$NON-NLS-1$
        return hentEksaktResultat(query);
    }

    @Override
    public List<Oppdrag110> hentOppdrag110ForPeriodeOgFagområde(LocalDate fomDato, LocalDate tomDato, String fagområde) {
        Objects.requireNonNull(fomDato, "fomDato");
        Objects.requireNonNull(tomDato, "tomDato");
        Objects.requireNonNull(fagområde, "fagområde");
        if (fomDato.isAfter(tomDato)) {
            throw new IllegalArgumentException("Datointervall for økonomioppdrag må inneholde minst 1 dag");
        }

        return entityManager
                .createQuery(
                        "select o110 from Oppdrag110 as o110, Oppdragskontroll as ok where o110.oppdragskontroll.id = ok.id " +
                                " and ok.opprettetTidspunkt >= :fomTidspunkt and ok.opprettetTidspunkt < :tilTidspunkt " +
                                " and o110.kodeFagomraade = :fagomrade" +
                                " order by ok.opprettetTidspunkt, o110.opprettetTidspunkt, o110.avstemming115.noekkelAvstemming", Oppdrag110.class) //$NON-NLS-1$
                .setParameter("fomTidspunkt", fomDato.atStartOfDay()) //$NON-NLS-1$
                .setParameter("tilTidspunkt", tomDato.plusDays(1).atStartOfDay()) //$NON-NLS-1$
                .setParameter("fagomrade", fagområde)
                .getResultList(); //$NON-NLS-1$
    }

    @Override
    public Oppdragskontroll finnVentendeOppdrag(long behandlingId) {
        final LockModeType lockModeType = LockModeType.PESSIMISTIC_FORCE_INCREMENT;
        TypedQuery<Oppdragskontroll> query = entityManager.createQuery(
                "from Oppdragskontroll where behandlingId = :behandlingId " +
                        "and venterKvittering = :venterKvittering", Oppdragskontroll.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        query.setParameter("venterKvittering", true); //$NON-NLS-1$
        query.setLockMode(lockModeType);
        return hentEksaktResultat(query);
    }

    @Override
    public Optional<Oppdragskontroll> finnOppdragForBehandling(long behandlingId) {
        List<Oppdragskontroll> resultList = entityManager.createQuery(
                "from Oppdragskontroll where behandlingId = :behandlingId", Oppdragskontroll.class)//$NON-NLS-1$
                .setParameter("behandlingId", behandlingId)
                .getResultList();

        if (resultList.size() > 1) {
            throw new IllegalStateException("Utviklerfeil: Finnes mer enn en Oppdragskontroll for behandling " + behandlingId);
        }
        return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
    }

    @Override
    public List<Oppdragskontroll> finnAlleOppdragForSak(String saksnr) {
        return entityManager.createQuery(
                "from Oppdragskontroll where saksnummer = :saksnr", Oppdragskontroll.class)//$NON-NLS-1$
                .setParameter(SAKSNR, saksnr)
                .getResultList();
    }

    @Override
    public long lagre(Oppdragskontroll oppdragskontroll) {
        entityManager.persist(oppdragskontroll);
        oppdragskontroll.getOppdrag110Liste().forEach(this::lagre);
        entityManager.flush();
        return oppdragskontroll.getId();
    }

    private void lagre(Oppdrag110 oppdrag110) {
        entityManager.persist(oppdrag110);
        lagre(oppdrag110.getAvstemming115());
        oppdrag110.getOppdragsenhet120Liste().forEach(this::lagre);
        oppdrag110.getOppdragslinje150Liste().forEach(this::lagre);
    }

    private void lagre(Oppdragsenhet120 oppdragsenhet120) {
        entityManager.persist(oppdragsenhet120);
    }

    private void lagre(Avstemming115 avstemming115) {
        entityManager.persist(avstemming115);
    }

    private void lagre(Oppdragslinje150 oppdragslinje150) {
        entityManager.persist(oppdragslinje150);
        oppdragslinje150.getAttestant180Liste().forEach(this::lagre);
    }

    private void lagre(Attestant180 attestant180) {
        entityManager.persist(attestant180);
    }
}
