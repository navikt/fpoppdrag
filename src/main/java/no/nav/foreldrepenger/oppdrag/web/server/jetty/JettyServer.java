package no.nav.foreldrepenger.oppdrag.web.server.jetty;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.ee11.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee11.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee11.servlet.DefaultServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.ee11.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.oppdrag.web.app.konfig.ApiConfig;
import no.nav.foreldrepenger.oppdrag.web.app.konfig.InternalApiConfig;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.ServiceStarterListener;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.log.metrics.MetricsUtil;

public class JettyServer {

    private static final Environment ENV = Environment.current();
    private static final String APPLICATION = "jakarta.ws.rs.Application";

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
        konfigurerLogging();
        createDatasourceMigrer();
        start();
    }

    private static void createDatasourceMigrer() {
        var jdbc = hentEllerBeregnVerdiHvisMangler("defaultDS.url", "defaultDSconfig", "jdbc_url");
        var username = hentEllerBeregnVerdiHvisMangler("defaultDS.username", "defaultDS", "username");
        var password = hentEllerBeregnVerdiHvisMangler("defaultDS.password", "defaultDS", "password");
        var dataSource = no.nav.vedtak.felles.jpa.jdbc.DatasourceUtil.oracleDataSource(jdbc, username, password, 15);
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
        MetricsUtil.scrape(); // TODO: erstatt med kommende init
    }

    private void start() throws Exception {
        var server = new Server(getServerPort());
        server.setConnectors(createConnectors(server).toArray(new Connector[]{}));
        server.setHandler(createContext());
        server.start();
        server.join();
    }

    private List<Connector> createConnectors(Server server) {
        List<Connector> connectors = new ArrayList<>();
        var httpConnector = new ServerConnector(server, new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(getServerPort());
        connectors.add(httpConnector);
        return connectors;
    }

    private static HttpConfiguration createHttpConfiguration() {
        var httpConfig = new HttpConfiguration();
        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        return httpConfig;
    }

    private static ContextHandler createContext() {
        var ctx = new ServletContextHandler(CONTEXT_PATH, ServletContextHandler.NO_SESSIONS);

        // Sikkerhet
        ctx.setSecurityHandler(simpleConstraints());

        // Servlets
        registerDefaultServlet(ctx);
        registerServlet(ctx, 0, InternalApiConfig.API_URI, InternalApiConfig.class);
        registerServlet(ctx, 1, ApiConfig.API_URI, ApiConfig.class);

        // Starter tjenester
        ctx.addEventListener(new ServiceStarterListener());

        // Enable Weld + CDI
        ctx.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        ctx.addServletContainerInitializer(new CdiServletContainerInitializer());
        ctx.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        return ctx;
    }

    private static void registerDefaultServlet(ServletContextHandler context) {
        var defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, "/*");
    }

    private static void registerServlet(ServletContextHandler context, int prioritet, String path, Class<?> appClass) {
        var servlet = new ServletHolder(new ServletContainer());
        servlet.setName(appClass.getName());
        servlet.setInitOrder(prioritet);
        servlet.setInitParameter(APPLICATION, appClass.getName());
        context.addServlet(servlet, path + "/*");
    }

    private static ConstraintSecurityHandler simpleConstraints() {
        var handler = new ConstraintSecurityHandler();
        // Slipp gjennom kall fra plattform til JaxRs. Foreløpig kun behov for GET
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, InternalApiConfig.API_URI + "/*"));
        // Slipp gjennom til autentisering i JaxRs / auth-filter
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, ApiConfig.API_URI + "/*"));
        // Alt annet av paths og metoder forbudt - 403
        handler.addConstraintMapping(pathConstraint(Constraint.FORBIDDEN, "/*"));
        return handler;
    }

    private static ConstraintMapping pathConstraint(Constraint constraint, String path) {
        var mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(path);
        return mapping;
    }

    private Integer getServerPort() {
        return this.serverPort;
    }

}
