package no.nav.foreldrepenger.oppdrag.dbstoette;

import static no.nav.foreldrepenger.oppdrag.dbstoette.Databaseskjemainitialisering.*;
import static no.nav.foreldrepenger.oppdrag.dbstoette.Databaseskjemainitialisering.migrerUnittestSkjemaer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerAwareExtension extends no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerAwareExtension.class);

    static {
        if (!DBTestUtil.kjøresAvMaven()) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            migrerUnittestSkjemaer();
        }
        settPlaceholdereOgJdniOppslag();
    }

}
