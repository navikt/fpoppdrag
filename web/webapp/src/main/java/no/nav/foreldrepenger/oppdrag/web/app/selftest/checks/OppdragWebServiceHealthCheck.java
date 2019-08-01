package no.nav.foreldrepenger.oppdrag.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.oppdrag.OppdragSelftestConsumer;

@ApplicationScoped
public class OppdragWebServiceHealthCheck extends WebServiceHealthCheck {

    private OppdragSelftestConsumer selftestConsumer;

    public OppdragWebServiceHealthCheck() {
        // CDI
    }

    @Inject
    public OppdragWebServiceHealthCheck(OppdragSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected String getDescription() {
        return "Test av web service Oppdrag (Økonomi)";
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }

}
