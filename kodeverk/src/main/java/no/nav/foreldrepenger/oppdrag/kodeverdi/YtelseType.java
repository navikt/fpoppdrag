package no.nav.foreldrepenger.oppdrag.kodeverdi;

public enum YtelseType {
    ES, //engangsstønad
    FP, //foreldrepenger
    SVP, //svangerskapspenger
    SYKEPENGER,
    PLEIEPENGER_SYKT_BARN,
    PLEIEPENGER_NÆRSTÅENDE,
    OMSORGSPENGER,
    OPPLÆRINGSPENGER,
    ;

    public boolean erIkkeEngangsstønad() {
        return !ES.equals(this);
    }
}
