package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeEndring;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeFagområde;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

class SokosOppdragProxyKlientTest {

    private final RestClient restClient = mock(RestClient.class);
    private final SokosOppdragProxyKlient klient = new SokosOppdragProxyKlient(restClient,
        new RestConfig(TokenFlow.NO_AUTH_NEEDED, URI.create("http://localhost/api/osSimulerBeregningApi/simulerBeregning"), null, null));

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void leserEnvelopeOgBevarerVarselOgTomBeregning(int status) {
        svar(200, respons(status, """
            {"gjelderId":"12345678910   ","belopTotalt":123.45,"ukjentFelt":true,"periodeStoppnivaa":[]}
            """));
        var resultat = klient.utførSimulering(kontroll(), null, false);
        assertThat(resultat).hasSize(2);
        assertThat(resultat.getFirst().statusKode()).isEqualTo(status);
        assertThat(resultat.getFirst().beregning().gjelderId()).isEqualTo("12345678910");
        verify(restClient, times(2)).sendReturnUnhandledNoRetry(any());

        svar(200, respons(status, "null"));
        assertThat(klient.utførSimulering(kontroll(), null, false).getFirst().beregning()).isNull();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {8, 12, 99})
    void businessfeilOgManglendeStatusErIkkeTommeResultater(Integer status) {
        svar(200, respons(status, "null"));
        assertThatThrownBy(() -> klient.utførSimulering(kontroll(), null, false)).isInstanceOf(IntegrasjonException.class);
        verify(restClient).sendReturnUnhandledNoRetry(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{}", "{\"OsSimulerBeregningOperationResponse\":{}}", "not json"})
    void avviserManglendeEllerUgyldigEnvelope(String body) {
        svar(200, body);
        assertThatThrownBy(() -> klient.utførSimulering(kontroll(), null, false)).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {204, 400, 401, 403, 500})
    void httpFeilErIkkeTommeResultater(int status) {
        svar(status, "skal ikke tolkes som simuleringsresultat");
        assertThatThrownBy(() -> klient.utførSimulering(kontroll(), null, false)).isInstanceOf(IntegrasjonException.class);
    }

    @Test
    void forberederNedetidsprotokollen() {
        svar(503, "");
        assertThatThrownBy(() -> klient.utførSimulering(kontroll(), null, false))
            .isInstanceOfSatisfying(SokosOppdragNedetidException.class, feil -> {
                assertThat(feil.getStatusCode()).isEqualTo(503);
                assertThat(feil.getFeilkode()).isEqualTo("OPPDRAG_FORVENTET_NEDETID");
            });
    }

    @Test
    void leserFelteneSomSkalLagresFraNativeRespons() {
        svar(200, respons(0, """
            {"gjelderId":"12345678910 ","gjelderNavn":"SYNTETISK NAVN","belopTotalt":100.25,
              "periodeStoppnivaa":[{"beregningsPeriode":{"periodeFom":"2026-01-01","periodeTom":"2026-01-31"},
                "beregningStoppnivaa":{"kodeFagomraade":"FP ","utbetalesTilId":"12345678910 ","datoForfall":"2026-02-01 ",
                  "oppdragsId":9999999999,"feilkonto":false,"stoppnivaaDetaljer":[
                    {"faktiskFom":"2026-01-01 ","faktiskTom":"2026-01-31 ","belop":-100.25,"typeKlasse":"YTEL ",
                     "tilbakeforing":"J","trekkVedtakId":9999999999}]}}]}
            """));
        var beregning = klient.utførSimulering(kontroll(), null, false).getFirst().beregning();
        assertThat(beregning.periodeStoppnivaa()).containsExactly(
            new SokosSimuleringRespons.PeriodeStoppnivaa(new SokosSimuleringRespons.Stoppnivaa("FP", "12345678910", "2026-02-01",
                List.of(new SokosSimuleringRespons.Detaljer("2026-01-01", "2026-01-31", new BigDecimal("-100.25"), "YTEL")))));
    }

    @SuppressWarnings("unchecked")
    private void svar(int status, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(restClient.sendReturnUnhandledNoRetry(any())).thenReturn(response);
    }

    private static String respons(Integer status, String beregning) {
        return """
            {"OsSimulerBeregningOperationResponse":{"ZS1OCont":{"responsSimulering":{
              "statusKode":%s,"statusMelding":"syntetisk melding","beregning":%s
            }}}}
            """.formatted(status, beregning);
    }

    private static OppdragskontrollDto kontroll() {
        var oppdrag = new Oppdrag110Dto(KodeEndring.NY, KodeFagområde.FP, "SYNTHETIC", "12345678910", "Z123456", null, List.of());
        return new OppdragskontrollDto("42", List.of(oppdrag, oppdrag));
    }
}
