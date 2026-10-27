package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import static no.nav.vedtak.mapper.json.DefaultJsonMapper.fromJson;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeEndring;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeEndringLinje;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeFagområde;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeKlassifik;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.KodeStatusLinje;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.LukketPeriode;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Ompostering116Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdragslinje150Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Refusjonsinfo156Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.SatsDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.TypeSats;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.UtbetalingsgradDto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

class SokosSimuleringRequestMapperTest {

    @Test
    void mapperTilSokosWireformatMedSoapDefaultverdier() {
        var oppdrag = oppdrag(KodeFagområde.FP, List.of(linje("SYNTHETIC-2")));
        var request = SokosSimuleringRequestMapper.map(new OppdragskontrollDto("42", List.of(oppdrag)), null, false).getFirst();
        var forventet = """
            {"OsSimulerBeregningOperation":{"ZS1ICont":{"requestSimulering":{
              "simuleringsPeriode":{"datoSimulerFom":"","datoSimulerTom":""},
              "oppdrag":{"kodeEndring":"NY","kodeFagomraade":"FP","fagsystemId":"SYNTHETIC",
                "utbetFrekvens":"MND","oppdragGjelderId":"12345678910","datoOppdragGjelderFom":"2000-01-01","saksbehId":"Z123456"},
              "omposter":[{"ompostering":"J","datoOmposterFom":"2026-01-01"}],
              "enhetOppdrag":[{"typeEnhet":"BOS","enhet":"8020","datoEnhetFom":"1900-01-01"}],
              "oppdragslinje":[{"kodeEndringLinje":"ENDR","kodeStatusLinje":"OPPH","datoStatusFom":"2026-01-20",
                "vedtakId":"42","delytelseId":"SYNTHETIC-2","kodeKlassifik":"FPATORD",
                "datoVedtakFom":"2026-01-01","datoVedtakTom":"2026-01-31","sats":1200,"typeSats":"DAG",
                "brukKjoreplan":false,"saksbehId":"Z123456","henvisning":"42","utbetalesTilId":"12345678910",
                "refFagsystemId":"PREVIOUS","refDelytelseId":"PREVIOUS-1","refusjonsInfo":[],
                "gradOppdragslinje":[{"typeGrad":"UFOR","grad":80}],"attestant":[{"attestantId":"Z123456"}]}]
            }}}}
            """;
        assertThat(fromJson(toJson(request), Map.class)).isEqualTo(fromJson(forventet, Map.class));
    }

    @ParameterizedTest
    @CsvSource({"FP,FPREF,FP", "SVP,SVPREF,SVP", "REFUTG,FPREF,ES"})
    void utenInntrekkVelgerKunBrukerOgOverstyrerOmpostering(KodeFagområde bruker, KodeFagområde refusjon, YtelseType ytelse) {
        var kontroll = new OppdragskontrollDto("42", List.of(
            oppdrag(refusjon, List.of(linje("A-1"))),
            oppdrag(bruker, List.of(linje("A-10"), linje("A-2")))));

        var requester = SokosSimuleringRequestMapper.map(kontroll, ytelse, true);

        assertThat(requester).hasSize(1);
        var request = requester.getFirst().operation().container().requestSimulering();
        assertThat(request.oppdrag().kodeFagomraade()).isEqualTo(bruker.name());
        assertThat(request.oppdrag().kodeEndring()).isEqualTo("ENDR");
        assertThat(request.omposter()).containsExactly(new SokosSimuleringRequest.Omposter("N", ""));
        assertThat(request.oppdragslinje()).extracting(SokosSimuleringRequest.Oppdragslinje::delytelseId).containsExactly("A-2", "A-10");
        assertThat(kontroll.oppdrag().getLast().ompostering116().omPostering()).isTrue();
        assertThat(kontroll.oppdrag().getLast().kodeEndring()).isEqualTo(KodeEndring.NY);
    }

    @ParameterizedTest
    @CsvSource({"FPREF,true,true", "SVPREF,true,true", "FP,false,true", "REFUTG,false,false"})
    void bevarerYtelsesspesifikkeRegler(KodeFagområde fagområde, boolean harRefusjon, boolean harGrad) {
        var kontroll = new OppdragskontrollDto("42", List.of(oppdrag(fagområde, List.of(linje("A-1")))));
        var linje = SokosSimuleringRequestMapper.map(kontroll, null, false).getFirst()
            .operation().container().requestSimulering().oppdragslinje().getFirst();
        assertThat(linje.refusjonsInfo()).hasSize(harRefusjon ? 1 : 0);
        assertThat(linje.gradOppdragslinje()).hasSize(harGrad ? 1 : 0);
        if (harRefusjon) {
            assertThat(linje.refusjonsInfo().getFirst()).isEqualTo(
                new SokosSimuleringRequest.RefusjonsInfo("00999999999", "2026-01-31", "2026-01-01"));
        }
    }

    @Test
    void avviserForMangeLinjerOgManglendeBrukerUtenInntrekk() {
        var forStort = new OppdragskontrollDto("42", List.of(oppdrag(KodeFagområde.FP, Collections.nCopies(1401, linje("A-1")))));
        assertThatThrownBy(() -> SokosSimuleringRequestMapper.map(forStort, null, false)).isInstanceOf(IllegalArgumentException.class);
        var kunRefusjon = new OppdragskontrollDto("42", List.of(oppdrag(KodeFagområde.FPREF, List.of(linje("A-1")))));
        assertThatThrownBy(() -> SokosSimuleringRequestMapper.map(kunRefusjon, YtelseType.FP, true)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Oppdrag110Dto oppdrag(KodeFagområde fagområde, List<Oppdragslinje150Dto> linjer) {
        return new Oppdrag110Dto(KodeEndring.NY, fagområde, "SYNTHETIC", "12345678910", "Z123456",
            new Ompostering116Dto(true, LocalDate.of(2026, 1, 1), "2026-01-01-10.00.00.000000"), linjer);
    }

    private static Oppdragslinje150Dto linje(String delytelseId) {
        return new Oppdragslinje150Dto(KodeEndringLinje.ENDR, "42", delytelseId, KodeKlassifik.FPF_ARBEIDSTAKER,
            new LukketPeriode(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
            new SatsDto(1200), TypeSats.DAG, new UtbetalingsgradDto(80), KodeStatusLinje.OPPH, LocalDate.of(2026, 1, 20),
            "12345678910", "PREVIOUS-1", "PREVIOUS",
            new Refusjonsinfo156Dto(LocalDate.of(2026, 1, 31), "00999999999", LocalDate.of(2026, 1, 1)));
    }
}
