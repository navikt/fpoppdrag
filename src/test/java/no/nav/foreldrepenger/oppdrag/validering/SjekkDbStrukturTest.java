package no.nav.foreldrepenger.oppdrag.validering;

import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;
import no.nav.vedtak.felles.testutilities.db.AbstractOracleDbStrukturTest;

/**
 * Tester at alle migreringer følger standarder for navn og god praksis.
 */
@ExtendWith(JpaExtension.class)
class SjekkDbStrukturTest extends AbstractOracleDbStrukturTest {}
