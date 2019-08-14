package no.nav.foreldrepenger.oppdrag;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;

class OppdragSelftestConsumerImpl implements OppdragSelftestConsumer {

    private SimulerFpService port;
    private String endpointUrl;

    public OppdragSelftestConsumerImpl(SimulerFpService port, String endpointUrl) {
        this.port = port;
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void ping() {
        try {
            port.simulerBeregning(new SimulerBeregningRequest());
        } catch (SimulerBeregningFeilUnderBehandling simulerBeregningFeilUnderBehandling) {
            // Denne grensesnitte har ikke ping tjeneste. Så for å sjekke tilkobling mellom økonomi og fpoppdrag,
            // kalles vi simulerBeregning request med dummy SimulerBeregningRequest
            // Hvis har vi tilkobling problemer som SOAP fault fordi tjeneste er ikke opp, test skal feil
        }
    }

    @Override
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
