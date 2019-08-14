package no.nav.foreldrepenger.oppdrag;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.foreldrepenger.oppdrag.util.XmlStringFieldFikser;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class OppdragConsumerImpl implements OppdragConsumer {

    public static final String SERVICE_IDENTIFIER = "OppdragService";

    private SimulerFpService port;

    public OppdragConsumerImpl(SimulerFpService port) {
        this.port = port;
    }

    @Override
    public SimulerBeregningResponse hentSimulerBeregningResponse(SimulerBeregningRequest simulerBeregningRequest) throws SimulerBeregningFeilUnderBehandling {
        try {
            SimulerBeregningResponse response = port.simulerBeregning(simulerBeregningRequest);
            XmlStringFieldFikser.stripTrailingSpacesFromStrings(response);
            return response;
        } catch (SOAPFaultException e) {
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

}
