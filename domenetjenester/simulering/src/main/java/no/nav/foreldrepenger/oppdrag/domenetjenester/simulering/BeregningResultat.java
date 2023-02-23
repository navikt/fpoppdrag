package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BeregningResultat {
    private Oppsummering oppsummering;
    private Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottaker;

    BeregningResultat(Oppsummering oppsummering, Map<Mottaker, List<SimulertBeregningPeriode>> beregningPerMottaker) {
        Objects.requireNonNull(oppsummering, "oppsummering");
        Objects.requireNonNull(beregningPerMottaker, "beregningPerMottaker");
        this.oppsummering = oppsummering;
        this.beregningPerMottaker = beregningPerMottaker;
    }

    public Oppsummering getOppsummering() {
        return oppsummering;
    }

    public Map<Mottaker, List<SimulertBeregningPeriode>> getBeregningPerMottaker() {
        return beregningPerMottaker;
    }
}
