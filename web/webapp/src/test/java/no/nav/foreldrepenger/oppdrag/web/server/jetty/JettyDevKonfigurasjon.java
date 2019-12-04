package no.nav.foreldrepenger.oppdrag.web.server.jetty;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8474;
    private static int DEFAULT_DEV_SERVER_PORT = 8070;

    JettyDevKonfigurasjon(){
        super(DEFAULT_DEV_SERVER_PORT);
    }

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }
}
