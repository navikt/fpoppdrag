package no.nav.foreldrepenger.oppdrag;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class OppdragConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/system/os/eksponering/simulerFpServiceWSBinding.wsdl";
    private static final String NAMESPACE = "http://nav.no/system/os/eksponering/simulerFpServiceWSBinding";
    private static final QName SERVICE = new QName(NAMESPACE, "simulerFpService");
    private static final QName PORT = new QName(NAMESPACE, "simulerFpServicePort");

    private String endpointUrl; // NOSONAR

    @Inject
    public OppdragConsumerConfig(@KonfigVerdi("Oppdrag.service.url") String endpointUrl) {
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
        return factoryBean.create(SimulerFpService.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
