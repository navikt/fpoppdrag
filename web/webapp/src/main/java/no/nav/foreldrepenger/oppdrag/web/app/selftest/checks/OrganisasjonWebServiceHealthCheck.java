package no.nav.foreldrepenger.oppdrag.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonSelftestConsumer;

@ApplicationScoped
public class OrganisasjonWebServiceHealthCheck extends WebServiceHealthCheck {

    private OrganisasjonSelftestConsumer organisasjonSelftestConsumer;

    OrganisasjonWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public OrganisasjonWebServiceHealthCheck(OrganisasjonSelftestConsumer organisasjonSelftestConsumer) {
        this.organisasjonSelftestConsumer = organisasjonSelftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        organisasjonSelftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Organisasjon";
    }

    @Override
    protected String getEndpoint() {
        return organisasjonSelftestConsumer.getEndpointUrl();
    }
}
