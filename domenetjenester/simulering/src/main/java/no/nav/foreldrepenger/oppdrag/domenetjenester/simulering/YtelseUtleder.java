package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import no.nav.foreldrepenger.oppdrag.kodeverdi.Fagområde;
import no.nav.foreldrepenger.oppdrag.kodeverdi.YtelseType;

public class YtelseUtleder {

    public static YtelseType utledFor(Fagområde fagområde) {
        if (fagområde == null) {
            throw new NullPointerException("Fagområde kan ikke våre null.");
        }
        return switch (fagområde) {
            case REFUTG -> YtelseType.ES;
            case FP -> YtelseType.FP;
            case FPREF -> YtelseType.FP;
            case SVP -> YtelseType.SVP;
            case SVPREF -> YtelseType.SVP;
            case SP -> YtelseType.SYKEPENGER;
            case SPREF -> YtelseType.SYKEPENGER;
            case PB -> YtelseType.PLEIEPENGER_SYKT_BARN;
            case PBREF -> YtelseType.PLEIEPENGER_SYKT_BARN;
            case OOP -> YtelseType.PLEIEPENGER_SYKT_BARN;
            case OOPREF -> YtelseType.PLEIEPENGER_SYKT_BARN;
            case OM -> YtelseType.OMSORGSPENGER;
            case OMREF -> YtelseType.OMSORGSPENGER;
            case PN -> YtelseType.PLEIEPENGER_NÆRSTÅENDE;
            case PNREF -> YtelseType.PLEIEPENGER_NÆRSTÅENDE;
            case OPP -> YtelseType.OPPLÆRINGSPENGER;
            case OPPREF -> YtelseType.OPPLÆRINGSPENGER;
        };
    }
}
