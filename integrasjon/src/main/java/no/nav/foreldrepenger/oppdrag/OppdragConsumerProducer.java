package no.nav.foreldrepenger.oppdrag;

import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SECURITYCONTEXT_TIL_SAML;
import static no.nav.vedtak.sts.client.NAVSTSClient.StsClientType.SYSTEM_SAML;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerFpService;
import no.nav.vedtak.sts.client.NAVSTSClient;
import no.nav.vedtak.sts.client.StsConfigurationUtil;

@Dependent
public class OppdragConsumerProducer {

    private OppdragConsumerConfig consumerConfig;

    @Inject
    public void setConfig(OppdragConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public OppdragConsumer oppdragConsumer() {
        SimulerFpService port = wrapWithSts(consumerConfig.getPort(), SECURITYCONTEXT_TIL_SAML);
        return new OppdragConsumerImpl(port);
    }

    public OppdragSelftestConsumer oppdragSelftestConsumer() {
        SimulerFpService port = wrapWithSts(consumerConfig.getPort(), SYSTEM_SAML);
        return new OppdragSelftestConsumerImpl(port, consumerConfig.getEndpointUrl());
    }

    SimulerFpService wrapWithSts(SimulerFpService port, NAVSTSClient.StsClientType samlTokenType) {
        return StsConfigurationUtil.wrapWithSts(port, samlTokenType);
    }

}
