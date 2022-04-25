package no.nav.foreldrepenger.oppdrag.kodeverdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Fagområde {

    REFUTG, //engangsstønad
    FP, //foreldrepenger
    FPREF, //foreldrepenger arbeidsgiver
    SP, //sykepenger
    SPREF, //sykepenger_arbeidsgiver
    SVP, //svangerskapspenger
    SVPREF, //svangerskapspenger_arbeidsgiver
    PB, //pleiepenger_sykt_barn
    PBREF, //pleiepenger_sykt_barn_arbeidsgiver
    PN, //pleiepenger_nærstående
    PNREF, //pleiepenger_nærstående_arbeidsgiver
    OM, //omsorgspenger
    OMREF, //omsorgspenger_arbeidsgiver
    OPP, //opplæringspenger
    OPPREF, //opplæringspenger_arbeidsgiver
    OOP, //pleiepenger_v1
    OOPREF, //pleiepenger_v1_arbeidsgiver
    ;

    private static final Logger LOG = LoggerFactory.getLogger(Fagområde.class);

    public static Fagområde fraKode(String kode) {
        try {
            return Fagområde.valueOf(kode);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Finner ikke FagOmrådeKode for {}", kode);
            throw ex;
        }
    }

    public static Fagområde utledFra(YtelseType ytelseType) {
        return switch (ytelseType) {
            case FP -> Fagområde.FP;
            case SVP -> Fagområde.SVP;
            case ES -> Fagområde.REFUTG;
            default -> throw new IllegalArgumentException("Utvikler-feil: Mangler mapping mellom ytelsetype og FagOmrådeKode for bruker. Ytelsetype=" + ytelseType);
        };
    }
}
