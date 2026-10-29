package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdrag110Dto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.OppdragskontrollDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.simulering.request.Oppdragslinje150Dto;
import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

final class SokosSimuleringRequestMapper {

    private SokosSimuleringRequestMapper() {
    }

    static List<SokosSimuleringRequest> map(OppdragskontrollDto kontroll, YtelseType ytelseType, boolean utenInntrekk) {
        var requester = kontroll.oppdrag().stream()
            .filter(oppdrag -> !utenInntrekk || oppdrag.kodeFagomrade().name().equals(Fagområde.utledFra(ytelseType).name()))
            .filter(oppdrag -> !utenInntrekk || !oppdrag.oppdragslinje150Liste().isEmpty())
            .map(oppdrag -> mapOppdrag(oppdrag, kontroll.behandlingId(), utenInntrekk))
            .toList();
        if (utenInntrekk && requester.isEmpty()) {
            throw new IllegalArgumentException("Fant ikke oppdrag for bruker ved simulering uten inntrekk");
        }
        return requester;
    }

    private static SokosSimuleringRequest mapOppdrag(Oppdrag110Dto dto, String behandlingId, boolean utenInntrekk) {
        if (dto.oppdragslinje150Liste().size() > 1400) {
            throw new IllegalArgumentException("Sokos tillater maksimalt 1400 oppdragslinjer");
        }
        var oppdrag = new SokosSimuleringRequest.Oppdrag(utenInntrekk ? "ENDR" : dto.kodeEndring().name(),
            dto.kodeFagomrade().name(), dto.fagsystemId(), "MND", dto.oppdragGjelderId(), "2000-01-01", dto.saksbehId());
        var linjer = dto.oppdragslinje150Liste().stream()
            .sorted((a, b) -> sammenlignDelytelseId(a.delytelseId(), b.delytelseId()))
            .map(linje -> mapLinje(linje, dto, behandlingId))
            .toList();
        var request = new SokosSimuleringRequest.RequestSimulering(
            new SokosSimuleringRequest.SimuleringsPeriode("", ""), oppdrag, mapOmpostering(dto, utenInntrekk),
            List.of(new SokosSimuleringRequest.Enhet("BOS", "8020", "1900-01-01")), linjer);
        return new SokosSimuleringRequest(new SokosSimuleringRequest.Operation(new SokosSimuleringRequest.Container(request)));
    }

    private static List<SokosSimuleringRequest.Omposter> mapOmpostering(Oppdrag110Dto dto, boolean utenInntrekk) {
        if (utenInntrekk) {
            return List.of(new SokosSimuleringRequest.Omposter("N", ""));
        }
        var ompostering = dto.ompostering116();
        return ompostering == null ? List.of() : List.of(new SokosSimuleringRequest.Omposter(
            Boolean.TRUE.equals(ompostering.omPostering()) ? "J" : "N", dato(ompostering.datoOmposterFom())));
    }

    private static SokosSimuleringRequest.Oppdragslinje mapLinje(Oppdragslinje150Dto linje, Oppdrag110Dto oppdrag, String behandlingId) {
        var refusjon = linje.refusjonsinfo156();
        var refusjonsInfo = !oppdrag.kodeFagomrade().gjelderEngangsstønad() && oppdrag.kodeFagomrade().gjelderRefusjonTilArbeidsgiver()
            ? List.of(new SokosSimuleringRequest.RefusjonsInfo(refusjon.refunderesId(), dato(refusjon.maksDato()), dato(refusjon.datoFom())))
            : List.<SokosSimuleringRequest.RefusjonsInfo>of();
        var grader = !oppdrag.kodeFagomrade().gjelderEngangsstønad() && linje.utbetalingsgrad() != null
            ? List.of(new SokosSimuleringRequest.Grad("UFOR", linje.utbetalingsgrad().verdi()))
            : List.<SokosSimuleringRequest.Grad>of();
        return new SokosSimuleringRequest.Oppdragslinje(linje.kodeEndringLinje().name(),
            linje.gjelderOpphør() ? "OPPH" : null, dato(linje.datoStatusFom()), linje.vedtakId(),
            linje.delytelseId(), linje.kodeKlassifik().getKode(), dato(linje.getDatoVedtakFom()), dato(linje.getDatoVedtakTom()),
            BigDecimal.valueOf(linje.sats().verdi()), linje.typeSats().name(), false, oppdrag.saksbehId(), behandlingId,
            linje.utbetalesTilId(), linje.refFagsystemId(), linje.refDelytelseId(), refusjonsInfo, grader,
            List.of(new SokosSimuleringRequest.Attestant(oppdrag.saksbehId())));
    }

    private static int sammenlignDelytelseId(String a, String b) {
        if (a.contains("-") && b.contains("-")) {
            var prefix = a.substring(0, a.lastIndexOf('-')).compareTo(b.substring(0, b.lastIndexOf('-')));
            return prefix != 0 ? prefix : Long.compare(Long.parseLong(a.substring(a.lastIndexOf('-') + 1)),
                Long.parseLong(b.substring(b.lastIndexOf('-') + 1)));
        }
        return a.compareTo(b);
    }

    private static String dato(LocalDate dato) {
        return dato == null ? "" : dato.toString();
    }
}
