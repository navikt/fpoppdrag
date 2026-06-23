package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.oppdrag.web.app.konfig.ApiConfig;
import no.nav.foreldrepenger.oppdrag.web.app.konfig.InternalApiConfig;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.ServiceStarterListener;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.felles.jpa.jdbc.DatasourceUtil;
import no.nav.vedtak.log.metrics.MetricsUtil;
import no.nav.vedtak.server.jetty.DataSourceShutdownListener;
import no.nav.vedtak.server.jetty.JettyServerBuilder;

public class JettyServer {

    private static final Environment ENV = Environment.current();

    private static final String CONTEXT_PATH = ENV.getProperty("context.path", "/fpoppdrag");

    private final Integer serverPort;

    static void main() throws Exception {
        jettyServer().bootStrap();
    }

    private static JettyServer jettyServer() {
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    protected JettyServer(int serverPort) {
        this.serverPort = serverPort;
    }

    void bootStrap() throws Exception {
        MetricsUtil.init(); // Sett opp registry før andre kobler seg på
        konfigurerLogging();
        createDatasourceMigrer();
        start();
    }

    private static void createDatasourceMigrer() {
        var jdbc = hentEllerBeregnVerdiHvisMangler("defaultDS.url", "defaultDSconfig", "jdbc_url");
        var username = hentEllerBeregnVerdiHvisMangler("defaultDS.username", "defaultDS", "username");
        var password = hentEllerBeregnVerdiHvisMangler("defaultDS.password", "defaultDS", "password");
        var dataSource = DatasourceUtil.oracleDataSource(jdbc, username, password, 15);
        DataSourceHolder.initialize(dataSource);
        FlywayUtil.migrateLegacyOracle(dataSource, NamingStandard.DEFAULT_DS_MIGRATION_CLASSPATH);
    }

    /* Denne gir lazy loading og feiler ikke ved lokalt kjøring uten vault mount */
    private static String hentEllerBeregnVerdiHvisMangler(String key, String mappeNavn, String filNavn) {
        if (ENV.getProperty(key) == null) {
            System.getProperties().computeIfAbsent(key, _ -> VaultUtil.lesFilVerdi(mappeNavn, filNavn));
        }
        return ENV.getRequiredProperty(key);
    }

    /**
     * Vi bruker SLF4J + logback, Jersey brukes JUL for logging.
     * Setter opp en bridge til å få Jersey til å logge gjennom Logback også.
     */
    private static void konfigurerLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private void start() throws Exception {
        var server = JettyServerBuilder.builder()
            .port(serverPort)
            .contextPath(CONTEXT_PATH)
            .withForwardedRequestCustomizer()
            .addEventListener(new ServiceStarterListener())
            .addEventListener(new DataSourceShutdownListener(DataSourceHolder::close))
            .registerRestApp(InternalApiConfig.API_URI, InternalApiConfig.class)
            .registerRestApp(ApiConfig.API_URI, ApiConfig.class)
            .build();
        server.start();
        server.join();
    }
}
