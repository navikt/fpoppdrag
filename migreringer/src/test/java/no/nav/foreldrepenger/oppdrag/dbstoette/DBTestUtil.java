package no.nav.foreldrepenger.oppdrag.dbstoette;

import no.nav.foreldrepenger.konfig.Environment;

public final class DBTestUtil {
    private static final boolean isRunningUnderMaven = Environment.current().getProperty("maven.cmd.line.args") != null;

    public static boolean kjøresAvMaven() {
        return isRunningUnderMaven;
    }
}
