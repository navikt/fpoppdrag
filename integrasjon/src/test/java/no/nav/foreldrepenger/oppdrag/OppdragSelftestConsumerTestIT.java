package no.nav.foreldrepenger.oppdrag;

import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.vedtak.felles.testutilities.UnitTestConfiguration;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.log.mdc.MDCOperations;

@RunWith(CdiRunner.class)
public class OppdragSelftestConsumerTestIT {

    private OppdragSelftestConsumer oppdragSelftestConsumer;

    @Before
    public void setUp() throws Exception {
        Properties unitTestProperties = UnitTestConfiguration.getUnitTestProperties(OppdragSelftestConsumerTestIT.class.getResource("/oppdrag.properties").toURI());
        UnitTestConfiguration.loadToSystemProperties(unitTestProperties, false);
        MDCOperations.putCallId();
        OppdragConsumerProducer oppdragConsumerProducer = new OppdragConsumerProducer();
        OppdragConsumerConfig oppdragConsumerConfig = new OppdragConsumerConfig(unitTestProperties.getProperty("Oppdrag.service.url"));
        oppdragConsumerProducer.setConfig(oppdragConsumerConfig);
        oppdragSelftestConsumer = oppdragConsumerProducer.oppdragSelftestConsumer();
    }

    @Ignore
    @Test
    public void test_ping() {
        oppdragSelftestConsumer.ping();
    }
}
