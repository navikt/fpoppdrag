package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record SokosSimuleringRequest(@JsonProperty("OsSimulerBeregningOperation") Operation operation) {

    public record Operation(@JsonProperty("ZS1ICont") Container container) { }

    public record Container(RequestSimulering requestSimulering) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RequestSimulering(SimuleringsPeriode simuleringsPeriode,
                                    Oppdrag oppdrag,
                                    List<Omposter> omposter,
                                    List<Enhet> enhetOppdrag,
                                    List<Oppdragslinje> oppdragslinje) { }

    public record SimuleringsPeriode(String datoSimulerFom, String datoSimulerTom) { }

    public record Oppdrag(String kodeEndring, String kodeFagomraade, String fagsystemId,
                          String utbetFrekvens, String oppdragGjelderId,
                          String datoOppdragGjelderFom, String saksbehId) { }

    public record Omposter(String ompostering, String datoOmposterFom) { }

    public record Enhet(String typeEnhet, String enhet, String datoEnhetFom) { }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Oppdragslinje(String kodeEndringLinje, String kodeStatusLinje, String datoStatusFom,
                                String vedtakId, String delytelseId, String kodeKlassifik,
                                String datoVedtakFom, String datoVedtakTom, BigDecimal sats, String typeSats,
                                boolean brukKjoreplan, String saksbehId, String henvisning, String utbetalesTilId,
                                String refFagsystemId, String refDelytelseId,
                                List<RefusjonsInfo> refusjonsInfo, List<Grad> gradOppdragslinje,
                                List<Attestant> attestant) { }

    public record RefusjonsInfo(String refunderesId, String maksDato, String datoFom) { }

    public record Grad(String typeGrad, int grad) { }

    public record Attestant(String attestantId) { }
}
