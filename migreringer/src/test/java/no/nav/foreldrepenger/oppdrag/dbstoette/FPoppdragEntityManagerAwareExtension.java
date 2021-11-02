package no.nav.foreldrepenger.oppdrag.dbstoette;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;


public class FPoppdragEntityManagerAwareExtension extends EntityManagerAwareExtension {

    private static final Logger LOG = LoggerFactory.getLogger(FPoppdragEntityManagerAwareExtension.class);

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
        if (!DBTestUtil.kjøresAvMaven()) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            //Databaseskjemainitialisering.migrerForUnitTests();
        }
        Databaseskjemainitialisering.settJndiOppslagForUnitTests();
    }

}
