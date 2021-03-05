package no.nav.foreldrepenger.oppdrag.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.OppdragSelftestConsumer;

@ApplicationScoped
public class OppdragWebServiceHealthCheck implements HealthCheck {

    private OppdragSelftestConsumer selftestConsumer;

    public OppdragWebServiceHealthCheck() {
        // CDI
    }

    @Inject
    public OppdragWebServiceHealthCheck(OppdragSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    public String getDescription() {
        return "Test av web service Oppdrag (Økonomi)";
    }

    @Override
    public boolean isOK() {
        try {
            selftestConsumer.ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }

}
