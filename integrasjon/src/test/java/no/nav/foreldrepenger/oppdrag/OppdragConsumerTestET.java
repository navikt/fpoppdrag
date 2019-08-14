package no.nav.foreldrepenger.oppdrag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class OppdragConsumerTestET {

    private SimulerFpService mockOppdragService;

    private OppdragConsumer oppdragConsumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockOppdragService = Mockito.mock(SimulerFpService.class);
        oppdragConsumer = new OppdragConsumerImpl(mockOppdragService);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentSimulerBeregningResponse() throws Exception {
        when(mockOppdragService.simulerBeregning(any(SimulerBeregningRequest.class)))
                .thenThrow(opprettSOAPFaultException("feil"));
        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");
        oppdragConsumer.hentSimulerBeregningResponse(mock(SimulerBeregningRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
