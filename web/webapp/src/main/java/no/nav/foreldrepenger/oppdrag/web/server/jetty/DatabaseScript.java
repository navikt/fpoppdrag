package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseScript {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseScript.class);
    protected static final String SCHEMA_VERSION = "schema_version";

    private final DataSource dataSource;
    private final String locations;

    public DatabaseScript(DataSource dataSource, String locations) {
        this.dataSource = dataSource;
        this.locations = locations;
    }

    public void migrate() {
        var flyway = new Flyway(Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .table(SCHEMA_VERSION)
                .baselineOnMigrate(true)
        );
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            LOG.error("Feil under migrering av databasen.");
            throw e;
        }
    }
}
