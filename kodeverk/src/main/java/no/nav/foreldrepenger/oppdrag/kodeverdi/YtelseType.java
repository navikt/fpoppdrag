package no.nav.foreldrepenger.oppdrag.kodeverdi;

public enum YtelseType {
    /** engangsstønad **/
    ES,
    /** foreldrepenger **/
    FP,
    /** svangerskapspenger **/
    SVP,
    // De under lagres ikke i databasen
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
