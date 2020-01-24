package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.ws.WebServiceException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.oppdrag.OppdragConsumer;
import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.TpsTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.OppdragMapper;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.mapper.SimuleringResultatTransformer;
import no.nav.foreldrepenger.oppdrag.kodeverdi.FagOmrådeKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.KlasseKode;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringXml;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringXmlRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;
import no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag.ØkonomiKodeEndring;
import no.nav.system.os.eksponering.simulerfpservicewsbinding.SimulerBeregningFeilUnderBehandling;
import no.nav.system.os.entiteter.beregningskjema.Beregning;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaa;
import no.nav.system.os.entiteter.beregningskjema.BeregningStoppnivaaDetaljer;
import no.nav.system.os.entiteter.beregningskjema.BeregningsPeriode;
import no.nav.system.os.tjenester.simulerfpservice.feil.FeilUnderBehandling;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningRequest;
import no.nav.system.os.tjenester.simulerfpservice.simulerfpservicegrensesnitt.SimulerBeregningResponse;
import no.nav.vedtak.exception.TekniskException;

public class StartSimuleringTjenesteTest {

    private static final Long BEHANDLING_ID_1 = 42345L;
    private static final Long BEHANDLING_ID_2 = 87890L;
    private static final String AKTØR_ID = "12345678901";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private SimuleringXmlRepository simuleringXmlRepository = new SimuleringXmlRepository(repositoryRule.getEntityManager());
    private SimuleringRepository simuleringRepository = new SimuleringRepository(repositoryRule.getEntityManager());
    private OppdragConsumer oppdragConsumerMock = mock(OppdragConsumer.class);
    private TpsTjeneste tpsTjenesteMock = mock(TpsTjeneste.class);
    private SimuleringResultatTransformer resultatTransformer = new SimuleringResultatTransformer(tpsTjenesteMock);

    private StartSimuleringTjeneste simuleringTjeneste;
    private SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();

    @Before
    public void setup() {

        simuleringTjeneste = new StartSimuleringTjeneste(simuleringXmlRepository, simuleringRepository, oppdragConsumerMock, resultatTransformer, simuleringBeregningTjeneste);
        when(tpsTjenesteMock.hentAktørForFnr(any())).thenReturn(Optional.of(new AktørId(AKTØR_ID)));

        repositoryRule.getEntityManager().createQuery("DELETE from SimuleringXml s WHERE s.eksternReferanse.behandlingId IN (:behandlingId1, :behandlingId2)")
                .setParameter("behandlingId1", BEHANDLING_ID_1)
                .setParameter("behandlingId2", BEHANDLING_ID_2)
                .executeUpdate();
    }

    @Test
    public void test_skalKasteFeilVedUkjentEllerUgyldigXml() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPO-832562");

        simuleringTjeneste.startSimulering(BEHANDLING_ID_1, Collections.singletonList("abcd"));
    }

    // TODO lagringAvSimuleringXML er implementert vha to transaksjoner, som gjør at data blir liggende i test-datbasen
    boolean erPåJenkins() {
        return !simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID_1).isEmpty()
                || !simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID_2).isEmpty();
    }

    @Test
    public void test_skalLagreOppdragXmlVedGyldigXml() throws Exception {
        if (erPåJenkins()) {
            return;
        }

        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker.xml");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(lagRespons("12345678901", "12345"));

        simuleringTjeneste.startSimulering(BEHANDLING_ID_1, Collections.singletonList(xml));

        List<SimuleringXml> resultat = simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID_1);
        assertThat(resultat).isNotNull();
        assertThat(resultat).hasSize(1);
        SimuleringXml resultatXml = resultat.get(0);
        assertThat(resultatXml.getEksternReferanse()).isNotNull();
        assertThat(resultatXml.getRequestXml()).isNotEmpty();
        assertThat(resultatXml.getResponseXml()).isNotEmpty();
    }

    @Test
    public void test_skalLagreToOppdragXml() throws Exception {
        if (erPåJenkins()) {
            return;
        }
        String xml1 = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_3.xml");
        String xml2 = TestResourceLoader.loadXml("/xml/oppdrag_refusjon.xml");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(lagRespons("12345678912", "67890"));

        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Arrays.asList(xml1, xml2));

        List<SimuleringXml> resultat = simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID_2);
        assertThat(resultat).isNotNull();
        assertThat(resultat).hasSize(2);
        assertThat(resultat.get(0).getResponseXml()).isNotNull();
        assertThat(resultat.get(1).getResponseXml()).isNotNull();
    }

    @Test
    public void test_skal_kaste_exception_ved_FeilUnderBehandling_error_fra_simuleringtjeneste() throws Exception {
        if (erPåJenkins()) {
            return;
        }
        FeilUnderBehandling fault = new FeilUnderBehandling();
        fault.setErrorMessage("error");
        fault.setDateTimeStamp(LocalDateTime.now().toString());
        fault.setErrorType("error");
        fault.setRootCause("Rootcause");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any()))
                .thenThrow(new SimulerBeregningFeilUnderBehandling("ERROR", fault));

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPO-845125");

        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_2.xml");

        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Collections.singletonList(xml));
    }

    @Test
    public void test_skal_kaste_exception_ved_WebServiceException_ved_kall_til_simuleringtjeneste() throws Exception {
        if (erPåJenkins()) {
            return;
        }
        WebServiceException fault = new WebServiceException("Error", null);
        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenThrow(fault);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FPO-852145");

        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_2.xml");

        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Collections.singletonList(xml));
    }

    @Test
    @Ignore("FIXME: Enten må koden fikses eller testen, feiler med NullpointerException")
    public void test_skal_deaktivere_forrige_simulering_når_ny_simulering_gir_tomt_resultat() throws Exception {
        if (erPåJenkins()) {
            return;
        }
        String xml1 = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_2.xml");
        String xml2 = TestResourceLoader.loadXml("/xml/oppdrag_refusjon.xml");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(lagRespons("24153532444", "423535"));

        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Collections.singletonList(xml1));

        Optional<SimuleringGrunnlag> grunnlagOpt1 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2);
        assertThat(grunnlagOpt1).isPresent();
        assertThat(grunnlagOpt1.get().isAktiv()).isTrue();

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(null);
        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Collections.singletonList(xml2));

        Optional<SimuleringGrunnlag> grunnlagOpt2 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2);
        assertThat(grunnlagOpt2).isEmpty();
    }

    @Test
    public void test_skal_deaktiver_behandling_med_gitt_behandling() throws Exception {
        if (erPåJenkins()) {
            return;
        }
        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_2.xml");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(lagRespons("24153532444", "423535"));

        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Collections.singletonList(xml));

        Optional<SimuleringGrunnlag> grunnlagOpt1 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2);
        assertThat(grunnlagOpt1).isPresent();
        assertThat(grunnlagOpt1.get().isAktiv()).isTrue();

        simuleringTjeneste.kansellerSimulering(BEHANDLING_ID_2);
        Optional<SimuleringGrunnlag> grunnlagOpt2 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2);
        assertThat(grunnlagOpt2).isNotPresent();
    }

    @Test
    public void mapperFlereBeregningsresultatTilSammeMottaker() throws Exception {
        // Arrange
        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_2.xml");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(lagRespons("24153532444", "423535"));

        // Act - Skal gi to beregningsresultater til samme mottaker
        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Arrays.asList(xml, xml));

        // Assert
        Optional<SimuleringGrunnlag> grunnlagOpt = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2);
        assertThat(grunnlagOpt).isPresent();

        Set<SimuleringMottaker> mottakere = grunnlagOpt.get().getSimuleringResultat().getSimuleringMottakere();
        assertThat(mottakere).hasSize(1);

        // To beregningsresultat til samme mottaker med én postering hver
        assertThat(mottakere.iterator().next().getSimulertePosteringer()).hasSize(2);
    }

    @Test
    public void simulerer_for_bruker_uten_inntrekk_dersom_første_resultat_gir_feilutbetaling_og_inntrekk() throws Exception {
        // Arrange
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern(OppdragMapper.PATTERN);
        LocalDate forfallsdato = LocalDate.now().plusMonths(1).withDayOfMonth(20);
        LocalDate fom = forfallsdato.withDayOfMonth(1);
        String gjelderId = "24153532444";
        String fagsysId = "423535";
        SimulerBeregningResponse response = lagRespons(gjelderId, fagsysId, pattern.format(LocalDate.now()));

        // Legger til feilutbetaling
        String periodeFom = response.getResponse().getSimulering().getBeregningsPeriode().get(0).getPeriodeFom();
        String periodeTom = response.getResponse().getSimulering().getBeregningsPeriode().get(0).getPeriodeTom();
        BeregningStoppnivaa beregningStoppnivaa1 = response.getResponse().getSimulering().getBeregningsPeriode().get(0).getBeregningStoppnivaa().get(0);
        beregningStoppnivaa1.getBeregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(periodeFom, periodeTom, PosteringType.FEILUTBETALING, BigDecimal.valueOf(3500)));
        beregningStoppnivaa1.getBeregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(periodeFom, periodeTom, PosteringType.YTELSE, BigDecimal.valueOf(3500)));


        // Legger til inntrekk neste måned
        BeregningsPeriode beregningsPeriode = new BeregningsPeriode();
        beregningsPeriode.setPeriodeFom(pattern.format(fom));
        beregningsPeriode.setPeriodeTom(pattern.format(forfallsdato));
        response.getResponse().getSimulering().getBeregningsPeriode().add(beregningsPeriode);

        BeregningStoppnivaa stoppnivå = opprettBeregningStoppnivå(gjelderId, fagsysId, pattern.format(forfallsdato));
        beregningsPeriode.getBeregningStoppnivaa().add(stoppnivå);
        stoppnivå.getBeregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(pattern.format(fom), pattern.format(forfallsdato), PosteringType.YTELSE, BigDecimal.valueOf(23500)));
        stoppnivå.getBeregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(pattern.format(fom), pattern.format(forfallsdato), PosteringType.JUSTERING, BigDecimal.valueOf(-2786)));


        ArgumentCaptor<SimulerBeregningRequest> captor = ArgumentCaptor.forClass(SimulerBeregningRequest.class);
        when(oppdragConsumerMock.hentSimulerBeregningResponse(captor.capture())).thenReturn(response);

        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker.xml");

        // Act
        simuleringTjeneste.startSimulering(BEHANDLING_ID_1, Collections.singletonList(xml));

        // Assert
        verify(oppdragConsumerMock, times(2)).hentSimulerBeregningResponse(any());
        SimulerBeregningRequest request = captor.getValue();
        assertThat(request.getRequest().getOppdrag().getOmpostering().getOmPostering()).isEqualTo("N");
        assertThat(request.getRequest().getOppdrag().getKodeEndring()).isEqualTo(ØkonomiKodeEndring.ENDR.name());

        Optional<SimuleringGrunnlag> simuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_1);
        assertThat(simuleringGrunnlag).isPresent();

        Set<SimuleringMottaker> simuleringMottakere = simuleringGrunnlag.get().getSimuleringResultat().getSimuleringMottakere();
        assertThat(simuleringMottakere).hasSize(1);

        SimuleringMottaker mottaker = simuleringMottakere.iterator().next();
        assertThat(mottaker.getSimulertePosteringer()).hasSize(5);
        assertThat(mottaker.getSimulertePosteringerUtenInntrekk()).hasSize(5);

        List<SimuleringXml> simuleringXmlListe = simuleringXmlRepository.hentSimuleringXml(BEHANDLING_ID_1);
        assertThat(simuleringXmlListe).hasSize(2);
    }

    @Test
    public void bestemmerYtelseTypeOgLagrerDetPåSimuleringsGrunnlaget() throws Exception {
        // Arrange
        String xml = TestResourceLoader.loadXml("/xml/oppdrag_mottaker_2.xml");

        when(oppdragConsumerMock.hentSimulerBeregningResponse(any())).thenReturn(lagRespons("24153532444", "423535"));

        // Act
        simuleringTjeneste.startSimulering(BEHANDLING_ID_2, Collections.singletonList(xml));

        // Assert
        Optional<SimuleringGrunnlag> grunnlagOpt1 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2);
        assertThat(grunnlagOpt1).isPresent();
        assertThat(grunnlagOpt1.get().getYtelseType()).isEqualTo(YtelseType.FORELDREPENGER);
    }

    private SimulerBeregningResponse lagRespons(String gjelderId, String fagsysId) {
        return lagRespons(gjelderId, fagsysId, "2018-10-15");
    }

    private SimulerBeregningResponse lagRespons(String gjelderId, String fagsysId, String forfallsdato) {
        SimulerBeregningResponse response = new SimulerBeregningResponse();
        no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse innerResponse = new no.nav.system.os.tjenester.simulerfpservice.simulerfpserviceservicetypes.SimulerBeregningResponse();
        response.setResponse(innerResponse);

        Beregning beregning = new Beregning();
        innerResponse.setSimulering(beregning);

        beregning.setGjelderNavn("dummy");
        beregning.setGjelderId(gjelderId);
        beregning.setDatoBeregnet("2018-10-10");
        beregning.setKodeFaggruppe("DUMMY");
        beregning.setBelop(BigDecimal.valueOf(1234L));

        BeregningsPeriode beregningsPeriode = new BeregningsPeriode();
        beregning.getBeregningsPeriode().add(beregningsPeriode);

        beregningsPeriode.setPeriodeFom("2018-09-01");
        beregningsPeriode.setPeriodeTom("2018-09-31");

        BeregningStoppnivaa stoppnivaa = opprettBeregningStoppnivå(gjelderId, fagsysId, forfallsdato);
        beregningsPeriode.getBeregningStoppnivaa().add(stoppnivaa);

        BeregningStoppnivaaDetaljer stoppnivaaDetaljer = opprettStoppnivaaDetaljer("2018-10-10", "2018-11-11", PosteringType.YTELSE, BigDecimal.valueOf(12532L));
        stoppnivaa.getBeregningStoppnivaaDetaljer().add(stoppnivaaDetaljer);

        return response;
    }

    private BeregningStoppnivaa opprettBeregningStoppnivå(String gjelderId, String fagsysId, String forfallsdato) {
        BeregningStoppnivaa stoppnivaa = new BeregningStoppnivaa();

        stoppnivaa.setKodeFagomraade(FagOmrådeKode.FORELDREPENGER.getKode());
        stoppnivaa.setUtbetalesTilId(gjelderId);
        stoppnivaa.setUtbetalesTilNavn("asfasf");
        stoppnivaa.setBehandlendeEnhet("8052");
        stoppnivaa.setForfall(forfallsdato);
        stoppnivaa.setOppdragsId(1234L);
        stoppnivaa.setStoppNivaaId(BigInteger.ONE);
        stoppnivaa.setFagsystemId(fagsysId);
        stoppnivaa.setBilagsType("U");
        stoppnivaa.setFeilkonto(false);
        return stoppnivaa;
    }

    private BeregningStoppnivaaDetaljer opprettStoppnivaaDetaljer(String fom, String tom, PosteringType posteringType, BigDecimal beløp) {
        BeregningStoppnivaaDetaljer stoppnivaaDetaljer = new BeregningStoppnivaaDetaljer();

        stoppnivaaDetaljer.setBelop(BigDecimal.valueOf(12345L));
        stoppnivaaDetaljer.setFaktiskFom(fom);
        stoppnivaaDetaljer.setFaktiskTom(tom);
        stoppnivaaDetaljer.setKontoStreng("1235432");
        stoppnivaaDetaljer.setBehandlingskode("2");
        stoppnivaaDetaljer.setBelop(beløp);
        stoppnivaaDetaljer.setTrekkVedtakId(0L);
        stoppnivaaDetaljer.setStonadId("2018-12-12");
        stoppnivaaDetaljer.setTilbakeforing(false);
        stoppnivaaDetaljer.setLinjeId(BigInteger.valueOf(21423L));
        stoppnivaaDetaljer.setSats(BigDecimal.valueOf(2254L));
        stoppnivaaDetaljer.setTypeSats("DAG");
        stoppnivaaDetaljer.setAntallSats(BigDecimal.valueOf(2542L));
        stoppnivaaDetaljer.setSaksbehId("5323");
        stoppnivaaDetaljer.setUforeGrad(BigInteger.valueOf(100L));
        stoppnivaaDetaljer.setDelytelseId("3523");
        stoppnivaaDetaljer.setBostedsenhet("4643");
        stoppnivaaDetaljer.setKlassekode(KlasseKode.FPATORD.getKode());
        stoppnivaaDetaljer.setKlasseKodeBeskrivelse("lsdfskød");
        stoppnivaaDetaljer.setTypeKlasse(posteringType.getKode());
        stoppnivaaDetaljer.setTypeKlasseBeskrivelse("sfas");

        return stoppnivaaDetaljer;
    }

}
