package no.nav.foreldrepenger.oppdrag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = "PosteringType")
@DiscriminatorValue(PosteringType.DISCRIMINATOR)
public class PosteringType extends Kodeliste {

    private static final Logger LOGGER = LoggerFactory.getLogger(PosteringType.class);

    public static final String DISCRIMINATOR = "POSTERING_TYPE";

    public static final PosteringType YTELSE = new PosteringType("YTEL");
    public static final PosteringType FEILUTBETALING = new PosteringType("FEIL");
    public static final PosteringType FORSKUDSSKATT = new PosteringType("SKAT");
    public static final PosteringType JUSTERING = new PosteringType("JUST");
    public static final PosteringType UDEFINERT = new PosteringType("-");

    private static Map<String, PosteringType> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(YTELSE.getKode(), YTELSE);
        TILGJENGELIGE.put(FEILUTBETALING.getKode(), FEILUTBETALING);
        TILGJENGELIGE.put(FORSKUDSSKATT.getKode(), FORSKUDSSKATT);
        TILGJENGELIGE.put(JUSTERING.getKode(), JUSTERING);
    }

    PosteringType() {
        // Hibernate
    }

    private PosteringType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static PosteringType fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        LOGGER.debug("Mottok ukjent PosteringType: {}", kode);
        return UDEFINERT;
    }

}
