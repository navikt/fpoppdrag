package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

class DataSourceKonfig {

    private static final String CLASSPATH_DB_MIGRATION = "classpath:/db/migration/";
    protected static final Environment ENV = Environment.current();
    private final DBConnProp defaultDatasource;
    private final List<DBConnProp> dataSources;

    DataSourceKonfig() {
        defaultDatasource = new DBConnProp(createDatasource(), CLASSPATH_DB_MIGRATION + "defaultDS");
        dataSources = Collections.singletonList(defaultDatasource);
    }

    private DataSource createDatasource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ENV.getProperty("defaultDS.url"));
        config.setUsername(ENV.getProperty("defaultDS.username"));
        config.setPassword(ENV.getProperty("defaultDS.password")); // NOSONAR false positive

        config.setConnectionTimeout(1000);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(30);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    DBConnProp getDefaultDatasource() {
        return defaultDatasource;
    }

    List<DBConnProp> getDataSources() {
        return dataSources;
    }

    class DBConnProp {
        private final DataSource datasource;
        private final String migrationScripts;

        public DBConnProp(DataSource datasource, String migrationScripts) {
            this.datasource = datasource;
            this.migrationScripts = migrationScripts;
        }

        public DataSource getDatasource() {
            return datasource;
        }

        public String getMigrationScripts() {
            return migrationScripts;
        }
    }

}
