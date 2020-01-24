package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class TilkjentYtelseRepository {

    private EntityManager entityManager;

    TilkjentYtelseRepository() {
        // for CDI
    }

    @Inject
    public TilkjentYtelseRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Optional<TilkjentYtelseEntitet> hentTilkjentYtelse(long behandlingId) {
        TypedQuery<TilkjentYtelseEntitet> query = entityManager.createQuery(
                "from TilkjentYtelseEntitet where behandlingId = :behandlingId", TilkjentYtelseEntitet.class); //$NON-NLS-1$
        query.setParameter("behandlingId", behandlingId); //$NON-NLS-1$
        return hentUniktResultat(query);
    }

    public void lagre(TilkjentYtelseEntitet tilkjentYtelse) {
        Objects.requireNonNull(tilkjentYtelse, "tilkjentYtelse");
        Optional<TilkjentYtelseEntitet> finnesForBehandling = hentTilkjentYtelse(tilkjentYtelse.getBehandlingId());
        if (finnesForBehandling.isPresent()) {
            throw new IllegalStateException("Utviklerfeil: Finner eksisterende tilkjent ytelse på denne behandlingen: " + tilkjentYtelse.getBehandlingId());
        }
        entityManager.persist(tilkjentYtelse);
        TilkjentYtelseBehandlingInfo tyBehandlingInfo = tilkjentYtelse.getTilkjentYtelseBehandlingInfo();
        entityManager.persist(tyBehandlingInfo);
        tilkjentYtelse.getTilkjentYtelsePeriodeListe()
                .forEach(this::lagrePeriode);
        entityManager.flush();
    }

    private void lagrePeriode(TilkjentYtelsePeriode tilkjentYtelsePeriode) {
        entityManager.persist(tilkjentYtelsePeriode);
        tilkjentYtelsePeriode.getTilkjentYtelseAndelListe()
                .forEach(this::lagreAndel);
    }

    private void lagreAndel(TilkjentYtelseAndel tilkjentYtelseAndel) {
        entityManager.persist(tilkjentYtelseAndel);
        lagreFeriepenger(tilkjentYtelseAndel.getTilkjentYtelseFeriepenger());
    }

    private void lagreFeriepenger(List<TilkjentYtelseFeriepenger> tilkjentYtelseFeriepenger) {
        for (TilkjentYtelseFeriepenger fp : tilkjentYtelseFeriepenger) {
            entityManager.persist(fp);
        }
    }
}
