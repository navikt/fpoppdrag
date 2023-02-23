package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

public class SimulertBeregningResultat {
    private YtelseType gjelderYtelseType;

    private BeregningResultat beregningResultat;

    private BeregningResultat beregningResultatUtenInntrekk;

    public SimulertBeregningResultat(BeregningResultat beregningResultat, YtelseType gjelderYtelseType) {
        Objects.requireNonNull(beregningResultat);
        Objects.requireNonNull(gjelderYtelseType);
        this.beregningResultat = beregningResultat;
        this.gjelderYtelseType = gjelderYtelseType;
    }

    public BeregningResultat getBeregningResultat() {
        return beregningResultat;
    }


    void setBeregningResultatUtenInntrekk(BeregningResultat beregningResultatUtenInntrekk) {
        this.beregningResultatUtenInntrekk = beregningResultatUtenInntrekk;
    }

    public Optional<BeregningResultat> getBeregningResultatUtenInntrekk() {
        return Optional.ofNullable(beregningResultatUtenInntrekk);
    }

    public YtelseType getGjelderYtelseType() {
        return gjelderYtelseType;
    }
}
