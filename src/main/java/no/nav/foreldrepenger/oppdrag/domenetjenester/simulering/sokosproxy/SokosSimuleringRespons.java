package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SokosSimuleringRespons(@JsonProperty("OsSimulerBeregningOperationResponse") Operation operation) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Operation(@JsonProperty("ZS1OCont") Container container) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Container(Respons responsSimulering) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Respons(Integer statusKode, String statusMelding, Beregning beregning) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Beregning(String gjelderId, List<PeriodeStoppnivaa> periodeStoppnivaa) {
        public Beregning {
            gjelderId = trim(gjelderId);
            periodeStoppnivaa = Optional.ofNullable(periodeStoppnivaa).orElse(List.of());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PeriodeStoppnivaa(Stoppnivaa beregningStoppnivaa) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stoppnivaa(String kodeFagomraade, String utbetalesTilId, String datoForfall, List<Detaljer> stoppnivaaDetaljer) {
        public Stoppnivaa {
            kodeFagomraade = trim(kodeFagomraade);
            utbetalesTilId = trim(utbetalesTilId);
            datoForfall = trim(datoForfall);
            stoppnivaaDetaljer = Optional.ofNullable(stoppnivaaDetaljer).orElse(List.of());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detaljer(String faktiskFom, String faktiskTom, BigDecimal belop, String typeKlasse) {
        public Detaljer {
            faktiskFom = trim(faktiskFom);
            faktiskTom = trim(faktiskTom);
            typeKlasse = trim(typeKlasse);
        }
    }

    private static String trim(String verdi) {
        return verdi == null ? null : verdi.stripTrailing();
    }
}
