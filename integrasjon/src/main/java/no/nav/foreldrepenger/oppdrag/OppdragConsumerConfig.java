package no.nav.foreldrepenger.oppdrag;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;

@Dependent
public class OppdragConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/system/os/eksponering/simulerfpservicewsbinding.wsdl";
    private static final String NAMESPACE = "http://nav.no/system/os/eksponering/simulerFpServiceWSBinding";
    private static final QName SERVICE = new QName(NAMESPACE, "simulerFpService");
    private static final QName PORT = new QName(NAMESPACE, "simulerFpServicePort");

    private static final Environment ENV = Environment.current();

    private String endpointUrl; // NOSONAR

    @Inject
    public OppdragConsumerConfig(@KonfigVerdi("oppdrag.service.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    SimulerFpService getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(SimulerFpService.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());

        var port = factoryBean.create(SimulerFpService.class);

        if (!ENV.isProd()) {
            var client = ClientProxy.getClient(port);
            var loggingInInterceptor = new LoggingInInterceptor();
            loggingInInterceptor.setPrettyLogging(true);
            var loggingOutInterceptor = new LoggingOutInterceptor();
            loggingOutInterceptor.setPrettyLogging(true);
            client.getInInterceptors().add(loggingInInterceptor);
            client.getInFaultInterceptors().add(loggingInInterceptor);
            client.getOutInterceptors().add(loggingOutInterceptor);
            client.getOutFaultInterceptors().add(loggingInInterceptor);
        }
        return port;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
