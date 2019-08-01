package no.nav.foreldrepenger.oppdrag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = "FagOmraadeKode")
@DiscriminatorValue(FagOmrådeKode.DISCRIMINATOR)
public class FagOmrådeKode extends Kodeliste {

    private static final Logger LOGGER = LoggerFactory.getLogger(FagOmrådeKode.class);

    public static final String DISCRIMINATOR = "FAG_OMRAADE_KODE";

    public static final FagOmrådeKode FORELDREPENGER = new FagOmrådeKode("FP");
    public static final FagOmrådeKode FORELDREPENGER_ARBEIDSGIVER = new FagOmrådeKode("FPREF");
    public static final FagOmrådeKode SYKEPENGER = new FagOmrådeKode("SP");
    public static final FagOmrådeKode SYKEPENGER_ARBEIDSGIVER = new FagOmrådeKode("SPREF");
    public static final FagOmrådeKode PLEIEPENGER = new FagOmrådeKode("OOP");
    public static final FagOmrådeKode PLEIEPENGER_ARBEIDSGIVER = new FagOmrådeKode("OOPREF");
    public static final FagOmrådeKode ENGANGSSTØNAD = new FagOmrådeKode("REFUTG");
    public static final FagOmrådeKode SVANGERSKAPSPENGER = new FagOmrådeKode("SVP");
    public static final FagOmrådeKode SVANGERSKAPSPENGER_ARBEIDSGIVER = new FagOmrådeKode("SVPREF");
    public static final FagOmrådeKode UDEFINERT = new FagOmrådeKode("-");

    private static final Map<String, FagOmrådeKode> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(FORELDREPENGER.getKode(), FORELDREPENGER);
        TILGJENGELIGE.put(FORELDREPENGER_ARBEIDSGIVER.getKode(), FORELDREPENGER_ARBEIDSGIVER);
        TILGJENGELIGE.put(SYKEPENGER.getKode(), SYKEPENGER);
        TILGJENGELIGE.put(SYKEPENGER_ARBEIDSGIVER.getKode(), SYKEPENGER_ARBEIDSGIVER);
        TILGJENGELIGE.put(PLEIEPENGER.getKode(), PLEIEPENGER);
        TILGJENGELIGE.put(PLEIEPENGER_ARBEIDSGIVER.getKode(), PLEIEPENGER);
        TILGJENGELIGE.put(ENGANGSSTØNAD.getKode(), ENGANGSSTØNAD);
        TILGJENGELIGE.put(SVANGERSKAPSPENGER.getKode(), SVANGERSKAPSPENGER);
        TILGJENGELIGE.put(SVANGERSKAPSPENGER_ARBEIDSGIVER.getKode(), SVANGERSKAPSPENGER_ARBEIDSGIVER);
    }

    FagOmrådeKode() {
        //Hibernate
    }

    private FagOmrådeKode(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static FagOmrådeKode fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        LOGGER.debug("Mottok ukjent FagOmrådeKode: {}", kode);
        return UDEFINERT;
    }

    public static boolean gjelderForeldrepenger(String fagOmrådeKode) {
        return FORELDREPENGER.getKode().equals(fagOmrådeKode) || FORELDREPENGER_ARBEIDSGIVER.getKode().equals(fagOmrådeKode);
    }

    public static boolean gjelderEngangsstønad(String fagOmrådeKode) {
        return ENGANGSSTØNAD.getKode().equals(fagOmrådeKode);
    }

    public static boolean gjelderSvangerskapspenger(String fagOmrådeKode) {
        return SVANGERSKAPSPENGER.getKode().equals(fagOmrådeKode) || SVANGERSKAPSPENGER_ARBEIDSGIVER.getKode().equals(fagOmrådeKode);
    }

    public static FagOmrådeKode getFagOmrådeKodeForBrukerForYtelseType(YtelseType ytelseType) {
        if (ytelseType.gjelderEngangsstønad()) {
            return ENGANGSSTØNAD;
        }
        if (ytelseType.gjelderSvangerskapspenger()) {
            return SVANGERSKAPSPENGER;
        }
        if (ytelseType.gjelderForeldrePenger()) {
            return FORELDREPENGER;
        }
        if (ytelseType.equals(YtelseType.UDEFINERT)) {
            return FagOmrådeKode.UDEFINERT;
        }
        throw new IllegalArgumentException("Utvikler-feil: Mangler mapping mellom ytelsetype og fagområdekode for bruker. Ytelsetype=" + ytelseType);
    }
}
