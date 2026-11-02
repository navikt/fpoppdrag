package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeEndring;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeFagområde;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDetaljerDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningStoppnivåDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.respons.BeregningsPeriodeDto;
import no.nav.foreldrepenger.oppdrag.domenetjenester.person.PersonTjeneste;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.FpWsProxySimuleringKlient;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy.OppdragNedetidException;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy.SokosOppdragNedetidException;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy.SokosOppdragProxyKlient;
import no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy.SokosSimuleringRespons;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringGrunnlag;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.SimuleringRepository;
import no.nav.foreldrepenger.oppdrag.oppdragslager.simulering.typer.AktørId;

class StartSimuleringSokosTest {

    private final SimuleringRepository repository = mock(SimuleringRepository.class);
    private final FpWsProxySimuleringKlient wsProxy = mock(FpWsProxySimuleringKlient.class);
    private final SokosOppdragProxyKlient sokos = mock(SokosOppdragProxyKlient.class);
    private final PersonTjeneste personTjeneste = mock(PersonTjeneste.class);
    private final SimuleringResultatTransformer transformer = new SimuleringResultatTransformer(personTjeneste);
    private final SimuleringBeregningTjeneste beregningTjeneste = mock(SimuleringBeregningTjeneste.class);
    private final Oppsummering oppsummering = new Oppsummering();
    private final ListAppender<ILoggingEvent> logger = new ListAppender<>();
    private final Logger tjenesteLogger = (Logger) LoggerFactory.getLogger(StartSimuleringTjeneste.class);
    private Level opprinneligLoggnivå;
    private final OppdragskontrollDto kontroll = new OppdragskontrollDto("42", List.of(
        new Oppdrag110Dto(KodeEndring.NY, KodeFagområde.FP, "SYNTHETIC", "12345678910", "Z123456", null, List.of())));

    @BeforeEach
    void setup() {
        when(personTjeneste.hentAktørForFnr("12345678910")).thenReturn(Optional.of(new AktørId("1234567890123")));
        when(beregningTjeneste.hentBeregningsresultat(any())).thenReturn(new BeregningResultat(oppsummering, Map.of()));
        when(wsProxy.utførSimuleringMedExceptionHandling(kontroll, null, false)).thenReturn(List.of(soap("100")));
        opprinneligLoggnivå = tjenesteLogger.getLevel();
        tjenesteLogger.setLevel(Level.INFO);
        logger.start();
        tjenesteLogger.addAppender(logger);
    }

    @AfterEach
    void ryddLogger() {
        tjenesteLogger.detachAppender(logger);
        tjenesteLogger.setLevel(opprinneligLoggnivå);
        logger.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.00", "999"})
    void sammenlignerGrunnlagMenLagrerAlltidKunWsProxy(String sokosBeløp) {
        when(sokos.utførSimulering(kontroll, YtelseType.FP, false)).thenReturn(List.of(sokosRespons(sokosBeløp)));
        tjeneste(true).startSimulering(kontroll);

        assertThat(lagretBeløp()).isEqualByComparingTo("100");
        assertThat(meldinger()).anyMatch(m -> m.contains(sokosBeløp.equals("999") ? ": ulike grunnlag." : ": like grunnlag."));
        if (sokosBeløp.equals("999")) {
            assertThat(meldinger()).anyMatch(m -> m.contains("manglerISokos=1 ekstraISokos=1"));
        }
        assertThat(meldinger()).noneMatch(m -> m.contains("12345678910") || m.contains("1234567890123"));
        verify(sokos, never()).utførSimulering(any(), any(), eq(true));
    }

    @Test
    void sammenligningenKanSlåsAv() {
        tjeneste(false).startSimulering(kontroll);
        assertThat(lagretBeløp()).isEqualByComparingTo("100");
        verifyNoInteractions(sokos);
    }

    @Test
    void tomFasitDeaktivererUtenÅKalleSokos() {
        when(wsProxy.utførSimuleringMedExceptionHandling(kontroll, null, false)).thenReturn(List.of());
        tjeneste(true).startSimulering(kontroll);
        verify(repository).hentSimulertOppdragForBehandling(42L);
        verify(repository, never()).lagreSimuleringGrunnlag(any());
        verifyNoInteractions(sokos);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void wsProxyNedetidPropagererUendretOgsåUtenInntrekk(boolean utenInntrekk) {
        var feil = new OppdragNedetidException();
        if (utenInntrekk) {
            aktiverUtenInntrekk();
            when(wsProxy.utførSimuleringMedExceptionHandling(kontroll, YtelseType.FP, true)).thenThrow(feil);
        } else {
            when(wsProxy.utførSimuleringMedExceptionHandling(kontroll, null, false)).thenThrow(feil);
        }
        assertThatThrownBy(() -> tjeneste(true).startSimulering(kontroll)).isSameAs(feil);
        verifyNoInteractions(sokos, repository);
    }

    @Test
    void sokosNedetidPåvirkerIkkeAutoritativtResultat() {
        when(sokos.utførSimulering(kontroll, YtelseType.FP, false)).thenThrow(new SokosOppdragNedetidException());
        tjeneste(true).startSimulering(kontroll);
        assertThat(lagretBeløp()).isEqualByComparingTo("100");
        assertThat(meldinger()).anyMatch(m -> m.contains("SokosOppdragNedetidException"));
    }

    @Test
    void tomSokosResponsDeaktivererIkkeAutoritativtResultat() {
        when(sokos.utførSimulering(kontroll, YtelseType.FP, false))
            .thenReturn(List.of(new SokosSimuleringRespons.Respons(0, "", null)));
        tjeneste(true).startSimulering(kontroll);
        assertThat(lagretBeløp()).isEqualByComparingTo("100");
        assertThat(meldinger()).anyMatch(m -> m.contains("tomt resultat fra Sokos"));
    }

    @Test
    void mappingfeilFraSokosPåvirkerIkkeAutoritativtResultat() {
        when(sokos.utførSimulering(kontroll, YtelseType.FP, false)).thenReturn(List.of(new SokosSimuleringRespons.Respons(0, "",
            new SokosSimuleringRespons.Beregning("12345678910", List.of(new SokosSimuleringRespons.PeriodeStoppnivaa(null))))));
        tjeneste(true).startSimulering(kontroll);
        assertThat(lagretBeløp()).isEqualByComparingTo("100");
        assertThat(meldinger()).anyMatch(m -> m.contains("Sokos-sammenligning feilet"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void speilerUtenInntrekkUtenÅBlandeResultater(boolean sokosFeiler) {
        aktiverUtenInntrekk();
        when(wsProxy.utførSimuleringMedExceptionHandling(kontroll, YtelseType.FP, true)).thenReturn(List.of(soap("200")));
        when(sokos.utførSimulering(kontroll, YtelseType.FP, false)).thenReturn(List.of(sokosRespons("100.0")));
        if (sokosFeiler) {
            when(sokos.utførSimulering(kontroll, YtelseType.FP, true)).thenThrow(new SokosOppdragNedetidException());
        } else {
            when(sokos.utførSimulering(kontroll, YtelseType.FP, true)).thenReturn(List.of(sokosRespons("200.00")));
        }
        tjeneste(true).startSimulering(kontroll);

        var lagret = lagretGrunnlag().getSimuleringResultat().getSimuleringMottakere().iterator().next();
        assertThat(lagret.getSimulertePosteringer()).singleElement().satisfies(p -> assertThat(p.getBeløp()).isEqualByComparingTo("100"));
        assertThat(lagret.getSimulertePosteringerUtenInntrekk()).singleElement()
            .satisfies(p -> assertThat(p.getBeløp()).isEqualByComparingTo("200"));
        assertThat(meldinger()).anyMatch(m -> m.contains(sokosFeiler ? "Sokos-sammenligning feilet" : ": like grunnlag."));
        verify(sokos).utførSimulering(kontroll, YtelseType.FP, true);
    }

    private void aktiverUtenInntrekk() {
        oppsummering.setFeilutbetaling(BigDecimal.ONE);
        oppsummering.setInntrekkNesteUtbetaling(BigDecimal.ONE);
    }

    private StartSimuleringTjeneste tjeneste(boolean sammenlign) {
        return new StartSimuleringTjeneste(repository, wsProxy, sokos, transformer, beregningTjeneste, sammenlign);
    }

    private List<String> meldinger() {
        return logger.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    private BigDecimal lagretBeløp() {
        return lagretGrunnlag().getSimuleringResultat().getSimuleringMottakere().iterator().next().getSimulertePosteringer().getFirst().getBeløp();
    }

    private SimuleringGrunnlag lagretGrunnlag() {
        var captor = ArgumentCaptor.forClass(SimuleringGrunnlag.class);
        verify(repository).lagreSimuleringGrunnlag(captor.capture());
        return captor.getValue();
    }

    private static BeregningDto soap(String beløp) {
        return new BeregningDto.Builder().gjelderId("12345678910").beregningsPeriode(List.of(
            new BeregningsPeriodeDto("2026-01-01", "2026-01-31", List.of(new BeregningStoppnivåDto.Builder()
                .utbetalesTilId("12345678910").kodeFagomraade("FP").forfall("2026-01-31")
                .beregningStoppnivaaDetaljer(List.of(new BeregningStoppnivåDetaljerDto.Builder()
                    .faktiskFom("2026-01-01").faktiskTom("2026-01-31").belop(new BigDecimal(beløp)).typeKlasse("YTEL").build()))
                .build())))).build();
    }

    private static SokosSimuleringRespons.Respons sokosRespons(String beløp) {
        return new SokosSimuleringRespons.Respons(0, "", new SokosSimuleringRespons.Beregning("12345678910", List.of(
            new SokosSimuleringRespons.PeriodeStoppnivaa(new SokosSimuleringRespons.Stoppnivaa("FP", "12345678910", "2026-01-31",
                List.of(new SokosSimuleringRespons.Detaljer("2026-01-01", "2026-01-31", new BigDecimal(beløp), "YTEL")))))));
    }
}
