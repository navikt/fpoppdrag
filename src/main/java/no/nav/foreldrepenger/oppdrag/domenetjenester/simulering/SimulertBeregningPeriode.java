package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;

public class SimulertBeregningPeriode {
    private Periode periode;
    private BigDecimal resultatEtterMotregning = BigDecimal.ZERO;
    private BigDecimal inntrekkNesteMåned = BigDecimal.ZERO;
    private BigDecimal resultat = BigDecimal.ZERO;
    private Map<Fagområde, SimulertBeregning> beregningPerFagområde = new EnumMap<>(Fagområde.class);

    public Periode getPeriode() {
        return periode;
    }

    public Map<Fagområde, SimulertBeregning> getBeregningPerFagområde() {
        return beregningPerFagområde;
    }

    public BigDecimal getResultatEtterMotregning() {
        return resultatEtterMotregning;
    }

    public BigDecimal getInntrekkNesteMåned() {
        return inntrekkNesteMåned;
    }

    public BigDecimal getResultat() {
        return resultat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SimulertBeregningPeriode kladd = new SimulertBeregningPeriode();

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medBeregning(Fagområde fagOmrådeKode, SimulertBeregning simulertBeregning) {
            kladd.beregningPerFagområde.put(fagOmrådeKode, simulertBeregning);
            return this;
        }

        public Builder leggTilPåResultatEtterMotregning(BigDecimal resultatEtterMotregning) {
            kladd.resultatEtterMotregning = kladd.resultatEtterMotregning.add(resultatEtterMotregning);
            return this;
        }

        public Builder leggTilPåInntrekkNesteMåned(BigDecimal inntrekkNesteMåned) {
            kladd.inntrekkNesteMåned = kladd.inntrekkNesteMåned.add(inntrekkNesteMåned);
            return this;
        }

        public Builder leggTilPåResultat(BigDecimal resultat) {
            kladd.resultat = kladd.resultat.add(resultat);
            return this;
        }

        public SimulertBeregningPeriode build() {
            return kladd;
        }
    }
}
