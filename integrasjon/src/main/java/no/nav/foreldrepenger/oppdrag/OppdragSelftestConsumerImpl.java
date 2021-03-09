package no.nav.foreldrepenger.oppdrag;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;

class OppdragSelftestConsumerImpl implements OppdragSelftestConsumer {

    private String endpointUrl;

    public OppdragSelftestConsumerImpl(@SuppressWarnings("unused") SimulerFpService port, String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void ping() {
    }

    @Override
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
