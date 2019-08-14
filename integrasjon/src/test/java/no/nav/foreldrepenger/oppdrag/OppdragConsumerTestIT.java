package no.nav.foreldrepenger.oppdrag;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.entiteter.oppdragskjema.Attestant;
import no.nav.system.os.entiteter.oppdragskjema.Enhet;
import no.nav.system.os.entiteter.oppdragskjema.Grad;
import no.nav.system.os.entiteter.oppdragskjema.RefusjonsInfo;
import no.nav.system.os.entiteter.oppdragskjema.Valuta;
import no.nav.system.os.entiteter.typer.simpletypes.FradragTillegg;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdrag;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.Oppdragslinje;
import no.nav.vedtak.felles.testutilities.UnitTestConfiguration;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.log.mdc.MDCOperations;

@RunWith(CdiRunner.class)
public class OppdragConsumerTestIT {

    private OppdragConsumer oppdragConsumer;

    @Before
    public void setUp() throws Exception {
        Properties unitTestProperties = UnitTestConfiguration.getUnitTestProperties(OppdragConsumerTestIT.class.getResource("/oppdrag.properties").toURI());
        UnitTestConfiguration.loadToSystemProperties(unitTestProperties, false);
        MDCOperations.putCallId();
        OppdragConsumerConfig oppdragConsumerConfig = new OppdragConsumerConfig(unitTestProperties.getProperty("Oppdrag_service.url"));
        OppdragConsumerProducer oppdragConsumerProducer = new OppdragConsumerProducer();
        oppdragConsumerProducer.setConfig(oppdragConsumerConfig);
        oppdragConsumer = oppdragConsumerProducer.oppdragConsumer();
    }

    @Ignore
    @Test
    public void skal_hentSimulerBeregningResponse_med_gyldigRequest() throws SimulerBeregningFeilUnderBehandling {
        SimulerBeregningRequest simulerBeregningRequest = opprettSimuleringBeregningRequest();
        SimulerBeregningResponse simulerBeregningResponse = oppdragConsumer.hentSimulerBeregningResponse(simulerBeregningRequest);
        Assertions.assertThat(simulerBeregningResponse).isNotNull();
        Assertions.assertThat(simulerBeregningResponse.getResponse().getSimulering()).isNotNull();
        Assertions.assertThat(simulerBeregningResponse.getResponse().getSimulering().getBeregningsPeriode()).isNotEmpty();
    }

    @Ignore
    @Test
    public void skal_feil_hentSimulerBeregningResponse_med_ugyldigFagomraadeKode() {
        SimulerBeregningRequest simulerBeregningRequest = opprettSimuleringBeregningRequest();
        Oppdrag oppdrag = simulerBeregningRequest.getRequest().getOppdrag();
        oppdrag.setKodeFagomraade(null);
        simulerBeregningRequest.getRequest().setOppdrag(oppdrag);
        try {
            oppdragConsumer.hentSimulerBeregningResponse(simulerBeregningRequest);
        } catch (Exception e) {
            Assertions.assertThat(e).isInstanceOf(SimulerBeregningFeilUnderBehandling.class);
            SimulerBeregningFeilUnderBehandling simulerFeil = (SimulerBeregningFeilUnderBehandling) e;
            Assertions.assertThat(simulerFeil.getFaultInfo()).isNotNull();
            Assertions.assertThat(simulerFeil.getFaultInfo().getErrorMessage())
                    .isEqualToIgnoringCase("KODE-FAGOMRAADE er ikke utfylt");
        }
    }

    private SimulerBeregningRequest opprettSimuleringBeregningRequest() {
        Oppdrag oppdrag = opprettOppdrag();
        SimulerBeregningRequest request = new SimulerBeregningRequest();
        no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest simulerBeregningRequest = new
                no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningRequest();
        simulerBeregningRequest.setOppdrag(oppdrag);
        request.setRequest(simulerBeregningRequest);
        return request;
    }

    private Oppdrag opprettOppdrag() {
        Oppdrag oppdrag = new Oppdrag();
        oppdrag.setKodeEndring("NY");
        oppdrag.setKodeFagomraade("FPREF");
        oppdrag.setOppdragGjelderId("15088011020");
        oppdrag.setDatoOppdragGjelderFom(LocalDate.of(2000, 1, 1).toString());
        oppdrag.setSaksbehId("z991620");
        oppdrag.setFagsystemId("130158784102");
        oppdrag.setUtbetFrekvens("MND");

        Enhet enhet = new Enhet();
        enhet.setDatoEnhetFom(LocalDate.of(1900, 1, 1).toString());
        enhet.setEnhet("8020");
        enhet.setTypeEnhet("BOS");
        oppdrag.getEnhet().add(enhet);

        oppdrag.getOppdragslinje().add(fylleOppdragslinje());

        return oppdrag;
    }

    private Oppdragslinje fylleOppdragslinje() {
        Oppdragslinje oppdragslinje = new Oppdragslinje();
        oppdragslinje.setBrukKjoreplan("N");
        oppdragslinje.setKodeEndringLinje("NY");
        oppdragslinje.setKodeKlassifik("FPATFRI");
        oppdragslinje.setDatoVedtakFom(LocalDate.of(2018, 5, 11).toString());
        oppdragslinje.setDatoVedtakTom(LocalDate.of(2018, 9, 30).toString());
        oppdragslinje.setSats(new BigDecimal(1846));
        oppdragslinje.setFradragTillegg(FradragTillegg.T);
        oppdragslinje.setTypeSats("DAG");
        oppdragslinje.setSaksbehId("z991620");
        oppdragslinje.setUtbetalesTilId("15118005764");
        oppdragslinje.setVedtakId(LocalDate.of(2018, 8, 16).toString());
        oppdragslinje.setDelytelseId("135698843100100");
        oppdragslinje.setHenvisning("1060805");

        RefusjonsInfo refusjonsInfo = new RefusjonsInfo();
        refusjonsInfo.setRefunderesId("00973861778");
        refusjonsInfo.setDatoFom(LocalDate.now().toString());
        refusjonsInfo.setMaksDato(LocalDate.of(2019, 5, 1).toString());
        oppdragslinje.setRefusjonsInfo(refusjonsInfo);

        Grad grad = new Grad();
        grad.setGrad(BigInteger.valueOf(100L));
        grad.setTypeGrad("UFOR");
        oppdragslinje.getGrad().add(grad);

        Attestant attestant = new Attestant();
        attestant.setAttestantId("Z990157");
        attestant.setDatoUgyldigFom(LocalDate.of(2020, 1, 1).toString());
        oppdragslinje.getAttestant().add(attestant);

        Valuta valuta = new Valuta();
        valuta.setDatoValutaFom(LocalDate.now().toString());
        valuta.setFeilreg("");
        valuta.setTypeValuta("UTB");
        valuta.setValuta("NOK");
        oppdragslinje.getValuta().add(valuta);
        return oppdragslinje;
    }


}