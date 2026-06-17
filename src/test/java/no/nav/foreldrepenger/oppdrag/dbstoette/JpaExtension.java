package no.nav.foreldrepenger.oppdrag.dbstoette;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;
import no.nav.vedtak.felles.testutilities.db.MigrationUtil;

public class JpaExtension extends EntityManagerAwareExtension {

    public static String DEFAULT_TEST_DB_SCHEMA_NAME;
    private static final String TEST_DB_CONTAINER = Environment.current()
        .getProperty("testcontainer.test.db", String.class, "gvenzl/oracle-free:23-slim-faststart");

    static {
        initDatabase();
    }

    public static synchronized void initDatabase() {
        if (!DataSourceHolder.isInitialized()) {
            var testDatabase = new OracleContainer(DockerImageName.parse(TEST_DB_CONTAINER)).withReuse(true);
            testDatabase.start();
            DEFAULT_TEST_DB_SCHEMA_NAME = testDatabase.getUsername();
            var dataSource = MigrationUtil.createLocalBuildTestDataSource(testDatabase.getJdbcUrl(), testDatabase.getUsername(), testDatabase.getPassword());
            MigrationUtil.migrateLocalBuildTest(dataSource, NamingStandard.DEFAULT_DS_MIGRATION_CLASSPATH);
            DataSourceHolder.initialize(dataSource);
        }
    }
}
