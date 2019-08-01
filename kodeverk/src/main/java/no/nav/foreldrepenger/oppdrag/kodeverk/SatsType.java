package no.nav.foreldrepenger.oppdrag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = "SatsType")
@DiscriminatorValue(SatsType.DISCRIMINATOR)
public class SatsType extends Kodeliste {

    public static final String DISCRIMINATOR = "SATS_TYPE";
    public static final SatsType DAG = new SatsType("DAG");
    public static final SatsType UKE = new SatsType("UKE");
    public static final SatsType MND = new SatsType("MND");
    public static final SatsType ÅR = new SatsType("AAR");
    public static final SatsType ENGANG = new SatsType("ENG");
    public static final SatsType AKTO = new SatsType("AKTO");
    public static final SatsType UDEFINERT = new SatsType("-");
    private static final Logger LOGGER = LoggerFactory.getLogger(SatsType.class);
    private static Map<String, SatsType> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(DAG.getKode(), DAG);
        TILGJENGELIGE.put(UKE.getKode(), UKE);
        TILGJENGELIGE.put(MND.getKode(), MND);
        TILGJENGELIGE.put(ÅR.getKode(), ÅR);
        TILGJENGELIGE.put(ENGANG.getKode(), ENGANG);
        TILGJENGELIGE.put(AKTO.getKode(), AKTO);
    }

    SatsType() {
        // Hibernate
    }

    private SatsType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static SatsType fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        LOGGER.debug("Mottok ukjent SatsType: {}", kode);
        return UDEFINERT;
    }
}
