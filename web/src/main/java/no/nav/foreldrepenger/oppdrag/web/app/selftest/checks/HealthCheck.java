package no.nav.foreldrepenger.oppdrag.web.app.selftest.checks;

public interface HealthCheck {

    String getDescription();

    String getEndpoint();

    boolean isOK();

}
