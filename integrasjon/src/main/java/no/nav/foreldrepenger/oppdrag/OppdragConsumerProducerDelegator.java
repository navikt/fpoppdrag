package no.nav.foreldrepenger.oppdrag;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class OppdragConsumerProducerDelegator {

    private OppdragConsumerProducer producer;

    @Inject
    public OppdragConsumerProducerDelegator(OppdragConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public OppdragConsumer oppdragConsumerForEndUser() {
        return producer.oppdragConsumer();
    }

    @Produces
    public OppdragSelftestConsumer oppdragSelftestConsumerForSystemUser() {
        return producer.oppdragSelftestConsumer();
    }
}
