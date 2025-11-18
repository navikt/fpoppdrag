package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.oppdrag.dbstoette.JpaExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeEndring;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeEndringLinje;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeFagområde;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeKlassifik;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeStatusLinje;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.LukketPeriode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdragslinje150Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Refusjonsinfo156Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.SatsDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.TypeSats;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.UtbetalingsgradDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDetaljerDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningsPeriodeDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.FpWsProxySimuleringKlient;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.PosteringType;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringMottaker;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

@ExtendWith(JpaExtension.class)
public class StartSimuleringTjenesteTest {

    private static final String  BEHANDLING_ID_1 = "42345";
    private static final String BEHANDLING_ID_2 = "87890";
    private static final Long BEHANDLING_ID_1_L = 42345L;
    private static final Long BEHANDLING_ID_2_L = 87890L;
    private static final String AKTØR_ID = "1234567890123";

    private final FpWsProxySimuleringKlient fpWsProxySimuleringKlient = mock(FpWsProxySimuleringKlient.class);
    private final PersonTjeneste tpsTjenesteMock = mock(PersonTjeneste.class);
    private final SimuleringResultatTransformer resultatTransformer = new SimuleringResultatTransformer(tpsTjenesteMock);
    private final SimuleringBeregningTjeneste simuleringBeregningTjeneste = new SimuleringBeregningTjeneste();
    private SimuleringRepository simuleringRepository;
    private StartSimuleringTjeneste simuleringTjeneste;

    @BeforeEach
    public void setup(EntityManager entityManager) {

        simuleringRepository = new SimuleringRepository(entityManager);
        simuleringTjeneste = new StartSimuleringTjeneste(simuleringRepository, fpWsProxySimuleringKlient, resultatTransformer, simuleringBeregningTjeneste);
        when(tpsTjenesteMock.hentAktørForFnr(any())).thenReturn(Optional.of(new AktørId(AKTØR_ID)));
    }

    @Test
    void test_skal_deaktivere_forrige_simulering_når_ny_simulering_gir_tomt_resultat() throws Exception {
        var oppdrag1 = lagOppdrag("130158784200", "12345678910");
        var oppdragskontrollSimulering1 = new OppdragskontrollDto(BEHANDLING_ID_2, List.of(oppdrag1));
        when(fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(any(), any(), anyBoolean())).thenReturn(lagRespons(
            oppdragskontrollSimulering1));
        simuleringTjeneste.startSimulering(oppdragskontrollSimulering1);

        Optional<SimuleringGrunnlag> grunnlagOpt1 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2_L);
        assertThat(grunnlagOpt1).isPresent();
        assertThat(grunnlagOpt1.get().isAktiv()).isTrue();

        var oppdrag2 = lagOppdragRefusjon();
        var oppdragskontrollDtoSimulering2 = new OppdragskontrollDto(BEHANDLING_ID_2, List.of(oppdrag2));
        when(fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(any(), any(), anyBoolean())).thenReturn(null);
        simuleringTjeneste.startSimulering(oppdragskontrollDtoSimulering2);

        Optional<SimuleringGrunnlag> grunnlagOpt2 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2_L);
        assertThat(grunnlagOpt2).isEmpty();
    }

    @Test
    void test_skal_deaktiver_behandling_med_gitt_behandling() {
        var oppdrag1 = lagOppdrag("130158784200", "12345678910");
        OppdragskontrollDto oppdragskontrollDto = new OppdragskontrollDto(BEHANDLING_ID_2, List.of(oppdrag1));

        when(fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(any(), any(), anyBoolean())).thenReturn(lagRespons(oppdragskontrollDto));
        simuleringTjeneste.startSimulering(oppdragskontrollDto);

        Optional<SimuleringGrunnlag> grunnlagOpt1 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2_L);
        assertThat(grunnlagOpt1).isPresent();
        assertThat(grunnlagOpt1.get().isAktiv()).isTrue();

        simuleringTjeneste.kansellerSimulering(BEHANDLING_ID_2_L);
        Optional<SimuleringGrunnlag> grunnlagOpt2 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2_L);
        assertThat(grunnlagOpt2).isNotPresent();
    }

    @Test
    void mapperFlereBeregningsresultatTilSammeMottaker() {
        // Arrange
        var oppdrag1 = lagOppdrag("130158784200", "12345678910");
        var oppdragskontrollDto = new OppdragskontrollDto(BEHANDLING_ID_2, List.of(oppdrag1, oppdrag1));
        List<BeregningDto> mockRespons = lagRespons(oppdragskontrollDto);
        when(fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(any(), any(), anyBoolean())).thenReturn(mockRespons);

        // Act - Skal gi to beregningsresultater til samme mottaker
        simuleringTjeneste.startSimulering(oppdragskontrollDto);

        // Assert
        Optional<SimuleringGrunnlag> grunnlagOpt = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2_L);
        assertThat(grunnlagOpt).isPresent();

        Set<SimuleringMottaker> mottakere = grunnlagOpt.get().getSimuleringResultat().getSimuleringMottakere();
        assertThat(mottakere).hasSize(1);

        // To beregningsresultat til samme mottaker med én postering hver
        assertThat(mottakere.iterator().next().getSimulertePosteringer()).hasSize(2);
    }

    @Test
    void simulerer_for_bruker_uten_inntrekk_dersom_første_resultat_gir_feilutbetaling_og_inntrekk() {
        // Arrange
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate forfallsdato = LocalDate.now().plusMonths(1).withDayOfMonth(20);
        LocalDate fom = forfallsdato.withDayOfMonth(1);
        String gjelderId = "12345678910";
        String fagsysId = "423535";
        var oppdrag = lagOppdrag(fagsysId, gjelderId);
        var oppdragskontrollDto = new OppdragskontrollDto(BEHANDLING_ID_1, List.of(oppdrag));
        var response = lagRespons(pattern.format(LocalDate.now()), oppdragskontrollDto);

        // Legger til feilutbetaling
        var beregningsPeriodeDto = response.get(0).beregningsPeriode().get(0);
        var periodeFom = beregningsPeriodeDto.periodeFom();
        var periodeTom = beregningsPeriodeDto.periodeTom();
        var beregningStoppnivaa1 = beregningsPeriodeDto.beregningStoppnivaa().get(0);
        beregningStoppnivaa1.beregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(periodeFom, periodeTom, PosteringType.FEIL, BigDecimal.valueOf(3500)));
        beregningStoppnivaa1.beregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(periodeFom, periodeTom, PosteringType.YTEL, BigDecimal.valueOf(3500)));


        // Legger til inntrekk neste måned
        List<BeregningStoppnivåDto> beregningStoppnivåDtoListe = new ArrayList<>();
        var beregningsPeriode = new BeregningsPeriodeDto(
                pattern.format(fom),
                pattern.format(forfallsdato),
                beregningStoppnivåDtoListe);
        response.get(0).beregningsPeriode().add(beregningsPeriode);

        var stoppnivå = opprettBeregningStoppnivå(gjelderId, fagsysId, pattern.format(forfallsdato));
        beregningStoppnivåDtoListe.add(stoppnivå);
        stoppnivå.beregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(pattern.format(fom), pattern.format(forfallsdato), PosteringType.YTEL, BigDecimal.valueOf(23500)));
        stoppnivå.beregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer(pattern.format(fom), pattern.format(forfallsdato), PosteringType.JUST, BigDecimal.valueOf(-2786)));


        when(fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(any(), any(), anyBoolean())).thenReturn(response);

        // Act
        simuleringTjeneste.startSimulering(oppdragskontrollDto);

        // Assert
        verify(fpWsProxySimuleringKlient, times(2)).utførSimuleringMedExceptionHandling(any(), any(), anyBoolean());

        Optional<SimuleringGrunnlag> simuleringGrunnlag = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_1_L);
        assertThat(simuleringGrunnlag).isPresent();

        Set<SimuleringMottaker> simuleringMottakere = simuleringGrunnlag.get().getSimuleringResultat().getSimuleringMottakere();
        assertThat(simuleringMottakere).hasSize(1);

        SimuleringMottaker mottaker = simuleringMottakere.iterator().next();
        assertThat(mottaker.getSimulertePosteringer()).hasSize(5);
        assertThat(mottaker.getSimulertePosteringerUtenInntrekk()).hasSize(5);
    }

    @Test
    void bestemmerYtelseTypeOgLagrerDetPåSimuleringsGrunnlaget() {
        // Arrange
        var oppdrag = lagOppdrag("130158784200", "12345678910");
        var oppdragskontrollDto = new OppdragskontrollDto(BEHANDLING_ID_2, List.of(oppdrag));
        when(fpWsProxySimuleringKlient.utførSimuleringMedExceptionHandling(any(), any(), anyBoolean())).thenReturn(lagRespons(oppdragskontrollDto));

        // Act
        simuleringTjeneste.startSimulering(oppdragskontrollDto);

        // Assert
        Optional<SimuleringGrunnlag> grunnlagOpt1 = simuleringRepository.hentSimulertOppdragForBehandling(BEHANDLING_ID_2_L);
        assertThat(grunnlagOpt1).isPresent();
        assertThat(grunnlagOpt1.get().getYtelseType()).isEqualTo(YtelseType.FP);
    }


    public static Oppdrag110Dto lagOppdrag(String fagsystemId, String oppdragGjelderId) { // Skal være relativt lik oppdrag_mottaker_2.xml
        List<Oppdragslinje150Dto> oppdragslinje150 = new ArrayList<>();
        oppdragslinje150.add(lagOppdragslinlje150(oppdragGjelderId, LocalDate.of(2018, 5, 11),
                        LocalDate.of(2018, 5, 31),
                        "130158784200100", "135702910101", "135702910101100", false));
        return new Oppdrag110Dto(
                KodeEndring.NY,
                KodeFagområde.FP,
                fagsystemId,
                oppdragGjelderId,
                "Z999999",
                null,
                oppdragslinje150
        );
    }

    private static Oppdrag110Dto lagOppdragRefusjon() { // Skal være relativt lik oppdrag_mottaker_2.xml
        List<Oppdragslinje150Dto> oppdragslinje150 = new ArrayList<>();
        oppdragslinje150.add(lagOppdragslinlje150("12345678910",
                        LocalDate.of(2017, 9, 7),
                        LocalDate.of(2017, 9, 9),
                        "130158784200100", null, null, true));
        oppdragslinje150.add(lagOppdragslinlje150("12345678910",
                        LocalDate.of(2017, 9, 10),
                        LocalDate.of(2017, 9, 28),
                        "135702910101101","135702910101", "135702910101100", true));
        oppdragslinje150.add(lagOppdragslinlje150("12345678910",
                        LocalDate.of(2017, 10, 1),
                        LocalDate.of(2017, 10, 10),
                        "135702910101101","135702910101", "135702910101101", true));
        oppdragslinje150.add(lagOppdragslinlje150("12345678910",
                        LocalDate.of(2018, 5, 1),
                        LocalDate.of(2018, 5, 31),
                        "135702910101101",null, null, true));
        return new Oppdrag110Dto(
                KodeEndring.NY,
                KodeFagområde.FPREF,
                "135702910101",
                "12345678999",
                "Z991097",
                null,
                oppdragslinje150
        );
    }

    private static Oppdragslinje150Dto lagOppdragslinlje150(String oppdragGjelderId, LocalDate datoVedtakFom, LocalDate datoVedtakTom,
                                                            String delytelseId, String refFagsystemId, String refDelytelseId, boolean refusjon) {
        return new Oppdragslinje150Dto(
                KodeEndringLinje.NY,
                "2018-08-16",
                delytelseId,
                KodeKlassifik.FPF_FRILANSER,
                new LukketPeriode(datoVedtakFom, datoVedtakTom),
                new SatsDto(738),
                TypeSats.DAG,
                new UtbetalingsgradDto(100),
                KodeStatusLinje.OPPH,
                LocalDate.of(2018, 5, 11),
                oppdragGjelderId,
                refDelytelseId,
                refFagsystemId,
                lagRefusjonsinfo156(refusjon)
        );
    }

    private static Refusjonsinfo156Dto lagRefusjonsinfo156(boolean refusjon) {
        if (refusjon) {
            return new Refusjonsinfo156Dto(LocalDate.of(2017, 10, 10), "12345678910", LocalDate.of(2017, 12, 13));
        }
        return null;
    }


    private List<BeregningDto> lagRespons(OppdragskontrollDto oppdragskontrollDto) {
        return lagRespons("2018-10-15", oppdragskontrollDto);
    }

    private List<BeregningDto> lagRespons(String forfallsdato, OppdragskontrollDto oppdragskontrollDto) {
        List<BeregningDto> beregningDtos = new ArrayList<>();
        for (var oppdrag : oppdragskontrollDto.oppdrag()) {
            List<BeregningStoppnivåDto> beregningStoppnivaa = new ArrayList<>();
            BeregningStoppnivåDto beregningStoppnivåDto = opprettBeregningStoppnivå(oppdrag.oppdragGjelderId(), oppdrag.fagsystemId(), forfallsdato);
            beregningStoppnivåDto.beregningStoppnivaaDetaljer().add(opprettStoppnivaaDetaljer("2018-10-10", "2018-11-11", PosteringType.YTEL, BigDecimal.valueOf(12532L)));
            beregningStoppnivaa.add(beregningStoppnivåDto);

            List<BeregningsPeriodeDto> beregningsPeriodeDtos = new ArrayList<>();
            beregningsPeriodeDtos.add(new BeregningsPeriodeDto("2018-09-01", "2018-09-31", beregningStoppnivaa));
            var beregning = new BeregningDto.Builder()
                    .gjelderId(oppdrag.oppdragGjelderId())
                    .gjelderNavn("dummy")
                    .datoBeregnet("2018-10-10")
                    .kodeFaggruppe(oppdrag.kodeFagomrade().name())
                    .belop(BigDecimal.valueOf(1234L))
                    .beregningsPeriode(beregningsPeriodeDtos)
                    .build();
            beregningDtos.add(beregning);
        }
        return beregningDtos;
    }

    private BeregningStoppnivåDto opprettBeregningStoppnivå(String gjelderId, String fagsysId, String forfallsdato) {
        return new BeregningStoppnivåDto.Builder()
                .kodeFagomraade(Fagområde.FP.name())
                .stoppNivaaId(BigInteger.ONE)
                .behandlendeEnhet("8052")
                .oppdragsId(1234L)
                .fagsystemId(fagsysId)
                .utbetalesTilId(gjelderId)
                .utbetalesTilNavn("asfasf")
                .bilagsType("U")
                .forfall(forfallsdato)
                .feilkonto(false)
                .beregningStoppnivaaDetaljer(new ArrayList<>())
                .build();
    }

    private BeregningStoppnivåDetaljerDto opprettStoppnivaaDetaljer(String fom, String tom, PosteringType posteringType, BigDecimal beløp) {
        return new BeregningStoppnivåDetaljerDto.Builder()
                .faktiskFom(fom)
                .faktiskTom(tom)
                .kontoStreng("1235432")
                .behandlingskode("2")
                .belop(beløp)
                .trekkVedtakId(0L)
                .stonadId("2018-12-12")
                .tilbakeforing(false)
                .linjeId(BigInteger.valueOf(21423L))
                .sats(BigDecimal.valueOf(2254L))
                .typeSats("DAG")
                .antallSats(BigDecimal.valueOf(2542L))
                .saksbehId("5323")
                .uforeGrad(BigInteger.valueOf(100L))
                .delytelseId("3523")
                .bostedsenhet("4643")
                .typeKlasse(posteringType.name())
                .typeKlasseBeskrivelse("sfas")
                .build();
    }

}
