package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.oppdrag.OppdragConsumer;
import no.nav.foreldrepenger.oppdrag.dbstoette.EntityManagerAwareExtension;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.SimuleringBeregningTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.StartSimuleringTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.SimuleringResultatTransformer;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.oppdrag.kodeverdi.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringXmlRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.BehandlingIdDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimulerOppdragDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringDto;
import no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto.SimuleringResultatDto;
import no.nav.system.os.entiteter.beregningskjema.Beregning;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer;
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(EntityManagerAwareExtension.class)
public class SimuleringRestTjenesteTest {

    private static final Long BEHANDLING_ID = 123456789L;
    private static final String AKTØR_ID = "1234567890135";

    private OppdragConsumer oppdragConsumerMock;

    private SimuleringRestTjeneste restTjeneste;


    @BeforeEach
    public void setup(EntityManager entityManager) {
        PersonTjeneste tpsTjenesteMock = mock(PersonTjeneste.class);
        when(tpsTjenesteMock.hentAktørForFnr(any())).thenReturn(Optional.of(new AktørId(AKTØR_ID)));
        SimuleringRepository simuleringRepository = new SimuleringRepository(entityManager);
        SimuleringXmlRepository simuleringXmlRepository = new SimuleringXmlRepository(entityManager);
        oppdragConsumerMock = mock(OppdragConsumer.class);
        SimuleringResultatTransformer resultatTransformer = new SimuleringResultatTransformer(tpsTjenesteMock);
        SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
        StartSimuleringTjeneste startSimuleringTjeneste = new StartSimuleringTjeneste(simuleringXmlRepository,
                simuleringRepository, oppdragConsumerMock, resultatTransformer, simuleringBeregningTjeneste);
        HentNavnTjeneste hentNavnTjeneste = mock(HentNavnTjeneste.class);
        SimuleringResultatTjeneste simuleringResultatTjeneste = new SimuleringResultatTjeneste(simuleringRepository,
                hentNavnTjeneste, simuleringBeregningTjeneste);
        restTjeneste = new SimuleringRestTjeneste(simuleringResultatTjeneste, startSimuleringTjeneste);
    }

    @Test
    public void returnererNullDersomSimuleringForBehandlingIkkeFinnes() {
        SimuleringDto simuleringDto = restTjeneste.hentSimuleringResultatMedOgUtenInntrekk(
                new BehandlingIdDto("12345"));
        assertThat(simuleringDto).isNull();

        SimuleringResultatDto simuleringResultatDto = restTjeneste.hentSimuleringResultat(new BehandlingIdDto("12345"));
        assertThat(simuleringResultatDto).isNull();
    }

    @Test
    public void test_skalReturnereFeilVedUgyldigOppdragXml() {
        SimulerOppdragDto oppdragDto = SimulerOppdragDto.lagDto(BEHANDLING_ID, Collections.singletonList("ugyldigXML"));

        assertThrows(TekniskException.class, () -> restTjeneste.startSimulering(oppdragDto));
    }

    @Test
    public void test_skalReturnereStatus200VedGyldigOppdragXml() throws Exception {
        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker.xml");
        SimulerOppdragDto oppdragDto = SimulerOppdragDto.lagDto(BEHANDLING_ID, Collections.singletonList(xml));

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any(SimulerBeregningRequest.class), any())).thenReturn(
                lagRespons());


        Response response = restTjeneste.startSimulering(oppdragDto);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private SimulerBeregningResponse lagRespons() {
        SimulerBeregningResponse sbr = new SimulerBeregningResponse();
        no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse innerSbr = new no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse();
        sbr.setResponse(innerSbr);

        Beregning beregning = new Beregning();
        innerSbr.setSimulering(beregning);

        beregning.setBelop(BigDecimal.valueOf(100.00));
        beregning.setKodeFaggruppe("FPATFRI");
        beregning.setDatoBeregnet("2018-10-10");
        beregning.setGjelderId("123456789");
        beregning.setGjelderNavn("Bob");

        beregning.getBeregningsPeriode().add(lagBeregningsPeriode());

        return sbr;
    }

    private BeregningsPeriode lagBeregningsPeriode() {
        BeregningsPeriode bp = new BeregningsPeriode();
        bp.setPeriodeTom("2018-11-01");
        bp.setPeriodeFom("2018-11-30");
        bp.getBeregningStoppnivaa().add(lagBeregningStoppNivå());
        return bp;
    }

    private BeregningStoppnivaa lagBeregningStoppNivå() {
        BeregningStoppnivaa bsn = new BeregningStoppnivaa();
        bsn.setStoppNivaaId(BigInteger.ONE);
        bsn.setUtbetalesTilNavn("bob");
        bsn.setKodeFagomraade(FagOmrådeKode.FORELDREPENGER.getKode());
        bsn.setUtbetalesTilId("123456789");
        bsn.setOppdragsId(123421L);
        bsn.setForfall("2018-12-15");
        bsn.setBehandlendeEnhet("8002");
        bsn.setFagsystemId(Fagsystem.FPSAK.getKode());
        bsn.setUtbetalesTilNavn("bob");
        bsn.setBilagsType("U");

        bsn.getBeregningStoppnivaaDetaljer().add(lagBeregningsStoppNivåDetaljer());

        return bsn;
    }

    private BeregningStoppnivaaDetaljer lagBeregningsStoppNivåDetaljer() {
        BeregningStoppnivaaDetaljer bsnd = new BeregningStoppnivaaDetaljer();
        bsnd.setKlassekode(KlasseKode.FPATFRI.getKode());
        bsnd.setBostedsenhet("8002");
        bsnd.setUforeGrad(BigInteger.ZERO);
        bsnd.setKravhaverId("123456789");
        bsnd.setTypeSats("DAG");
        bsnd.setSats(BigDecimal.valueOf(100.00));
        bsnd.setTilbakeforing(false);
        bsnd.setTrekkVedtakId(0L);
        bsnd.setBehandlingskode("2");
        bsnd.setKontoStreng("321422");
        bsnd.setKorrigering("");
        bsnd.setLinjeId(BigInteger.ONE);
        bsnd.setAntallSats(BigDecimal.valueOf(15.00));
        bsnd.setSaksbehId("48289");
        bsnd.setTypeKlasse(PosteringType.YTELSE.getKode());
        bsnd.setFaktiskFom("2018-11-11");
        bsnd.setFaktiskTom("2018-12-11");
        bsnd.setBelop(BigDecimal.valueOf(1000.00));
        bsnd.setKlasseKodeBeskrivelse("klassekode");
        bsnd.setTypeKlasseBeskrivelse("typeklasse");
        return bsnd;
    }

}
