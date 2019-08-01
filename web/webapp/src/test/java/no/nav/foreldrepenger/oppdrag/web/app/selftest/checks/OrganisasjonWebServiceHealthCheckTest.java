package no.nav.foreldrepenger.oppdrag.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonSelftestConsumer;

public class OrganisasjonWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.erstatter.org";
        OrganisasjonSelftestConsumer mockSelftestConsumer = mock(OrganisasjonSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        OrganisasjonWebServiceHealthCheck check = new OrganisasjonWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();
        Mockito.verify(mockSelftestConsumer).ping();
    }
}
