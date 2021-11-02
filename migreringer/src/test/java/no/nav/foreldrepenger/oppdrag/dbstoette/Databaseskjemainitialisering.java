package no.nav.foreldrepenger.oppdrag.dbstoette;

import static no.nav.foreldrepenger.oppdrag.dbstoette.DBTestUtil.kjøresAvMaven;

import java.io.File;

import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Initielt skjemaoppsett + migrering av unittest-skjemaer
 */
public final class Databaseskjemainitialisering {

    private static final Logger LOG = LoggerFactory.getLogger(Databaseskjemainitialisering.class);
    private static final Environment ENV = Environment.current();
    private static final String SCHEMA_VERSION = "schema_version";

    //public static final String URL_DEFAULT = "jdbc:oracle:thin:@127.0.0.1:1521:XE";

    public static final String URL_DEFAULT = "jdbc:h2:mem:test:DB_CLOSE_DELAY=-1;MODE=Oracle";
    public static final String DEFAULT_SCEHMA = "fpoppdrag";
    public static final String JUNIT_SCHEMA = "fpoppdrag_unit";
    public static final String DBA_SCHEMA = "vl_dba";
    public static final String DEFAULT_DS_NAME = "defaultDS";
    public static final String DBA_DS_NAME = "vl_dba";

    public static final DBProperties DBA_PROPERTIES = new DBProperties(DBA_DS_NAME, DBA_SCHEMA, dbaScriptLocation());
    public static final DBProperties JUNIT_PROPERTIES = dbProperties(DEFAULT_DS_NAME, JUNIT_SCHEMA);
    public static final DBProperties DEFAULT_PROPERTIES = dbProperties(DEFAULT_DS_NAME, DEFAULT_SCEHMA);

    public static void main(String[] args) {
        //brukes i mvn clean install
        //migrerForUnitTests();
    }

    public static void migrer() {
        migrer(DEFAULT_PROPERTIES);
    }

    public static void migrerForUnitTests() {
        //Må kjøres først for å opprette fpoppdrag_unit
        migrer(DBA_PROPERTIES);
        migrer(JUNIT_PROPERTIES);
    }

    public static DBProperties dbProperties(String dsName, String schema) {
        return new DBProperties(dsName, schema, getScriptLocation(dsName));
    }

    public static void settJndiOppslag() {
        settJndiOppslag(DEFAULT_PROPERTIES);
    }

    public static void settJndiOppslagForUnitTests() {
        settJndiOppslag(JUNIT_PROPERTIES);
    }

    private static void settJndiOppslag(DBProperties properties) {
        try {
            new EnvEntry("jdbc/" + properties.dsName(), properties.dataSource());
        } catch (Exception e) {
            throw new RuntimeException("Feil under registrering av Jndi-entry for default datasource", e);
        }
    }

    private static void migrer(DBProperties dbProperties) {
        LOG.info("Migrerer {}", dbProperties.schema());

        Flyway flyway = new Flyway(Flyway
                .configure()
                .baselineOnMigrate(true)
                .dataSource(dbProperties.dataSource())
                .table(SCHEMA_VERSION)
                .locations(dbProperties.scriptLocation())
                .cleanOnValidationError(true));

        if (!ENV.isLocal()) {
            throw new IllegalStateException("Forventer at denne migreringen bare kjøres lokalt");
        }
        flyway.migrate();
    }

    private static String getScriptLocation(String dsName) {
        if (kjøresAvMaven()) {
            return classpathScriptLocation(dsName);
        }
        return fileScriptLocation("migreringer/src/main/resources/db/migration/" + dsName);
    }

    private static String classpathScriptLocation(String dsName) {
        return "classpath:/db/migration/" + dsName;
    }

    private static String dbaScriptLocation() {
        if (kjøresAvMaven()) {
            return classpathScriptLocation("vl_dba");
        }
        return fileScriptLocation("migreringer/src/test/resources/db/migration/vl_dba");
    }

    private static String fileScriptLocation(String relativePath) {
        File baseDir = new File(".").getAbsoluteFile();
        File location = new File(baseDir, relativePath);
        while (!location.exists()) {
            baseDir = baseDir.getParentFile();
            if (baseDir == null || !baseDir.isDirectory()) {
                throw new IllegalArgumentException("Klarte ikke finne : " + baseDir);
            }
            location = new File(baseDir, relativePath);
        }
        return "filesystem:" + location.getPath();
    }

    private static HikariConfig hikariConfig(String dsName, String schema) {
        var cfg = new HikariConfig();
        var url = ENV.getProperty(dsName + ".url", URL_DEFAULT);
        var username = ENV.getProperty(dsName + ".username", schema);
        var password = ENV.getProperty(dsName + ".password", schema);
        cfg.setJdbcUrl(url);
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setConnectionTimeout(10000);
        cfg.setMinimumIdle(0);
        cfg.setMaximumPoolSize(4);
        //cfg.setDriverClassName(org.h2.Driver.class.getName());
        cfg.setAutoCommit(false);
        return cfg;
    }

    public static class DBProperties {

        private final String schema;
        private final String scriptLocation;
        private final String dsName;
        private HikariDataSource ds;

        private DBProperties(String dsName, String schema, String scriptLocation) {
            this.dsName = dsName;
            this.schema = schema;
            this.scriptLocation = scriptLocation;
        }

        public String dsName() {
            return dsName;
        }

        public String schema() {
            return schema;
        }

        public synchronized DataSource dataSource() {
            if (ds == null) {
                ds = new HikariDataSource(hikariConfig(dsName, schema));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> ds.close()));
            }
            return ds;
        }

        public String scriptLocation() {
            return scriptLocation;
        }

        @Override
        public String toString() {
            return "DBProperties{" + "schema='" + schema + '\'' + ", scriptLocation='" + scriptLocation + '\''
                    + ", dsName='" + dsName + '\'' + '}';
        }
    }
}
