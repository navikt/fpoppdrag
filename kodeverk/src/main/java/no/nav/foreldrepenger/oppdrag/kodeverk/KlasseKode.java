package no.nav.foreldrepenger.oppdrag.kodeverk;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity(name = "KlasseKode")
@DiscriminatorValue(KlasseKode.DISCRIMINATOR)
public class KlasseKode extends Kodeliste {

    private static final Logger LOGGER = LoggerFactory.getLogger(KlasseKode.class);

    public static final String DISCRIMINATOR = "KLASSE_KODE";

    public static final KlasseKode FPATAL = new KlasseKode("FPATAL");
    public static final KlasseKode FPATFER = new KlasseKode("FPATFER");
    public static final KlasseKode FPATFRI = new KlasseKode("FPATFRI");
    public static final KlasseKode FPATORD = new KlasseKode("FPATORD");
    public static final KlasseKode FPENAD_OP = new KlasseKode("FPENAD-OP");
    public static final KlasseKode FPENFOD_OP = new KlasseKode("FPENFOD-OP");
    public static final KlasseKode FPREFAGFER_IOP = new KlasseKode("FPREFAGFER-IOP");
    public static final KlasseKode FPREFAG_IOP = new KlasseKode("FPREFAG-IOP");
    public static final KlasseKode FPSNDDM_OP = new KlasseKode("FPSNDDM-OP");
    public static final KlasseKode FPSNDFI = new KlasseKode("FPSNDFI");
    public static final KlasseKode FPSNDJB_OP = new KlasseKode("FPSNDJB-OP");
    public static final KlasseKode FPSND_OP = new KlasseKode("FPSND-OP");
    public static final KlasseKode FSKTSKAT = new KlasseKode("FSKTSKAT");
    public static final KlasseKode KL_KODE_FEIL_KORTTID = new KlasseKode("KL_KODE_FEIL_KORTTID");
    public static final KlasseKode TBMOTOBS = new KlasseKode("TBMOTOBS");
    public static final KlasseKode SPSND100D1DAGPFI = new KlasseKode("SPSND100D1DAGPFI");
    public static final KlasseKode SPSND100D1DTRPFI = new KlasseKode("SPSND100D1DTRPFI");
    public static final KlasseKode UDEFINERT = new KlasseKode("-");

    private static final Map<String, KlasseKode> TILGJENGELIGE = new HashMap<>();

    static {
        TILGJENGELIGE.put(FPATAL.getKode(), FPATAL);
        TILGJENGELIGE.put(FPATFER.getKode(), FPATFER);
        TILGJENGELIGE.put(FPATFRI.getKode(), FPATFRI);
        TILGJENGELIGE.put(FPATORD.getKode(), FPATORD);
        TILGJENGELIGE.put(FPENAD_OP.getKode(), FPENAD_OP);
        TILGJENGELIGE.put(FPENFOD_OP.getKode(), FPENFOD_OP);
        TILGJENGELIGE.put(FPREFAGFER_IOP.getKode(), FPREFAGFER_IOP);
        TILGJENGELIGE.put(FPREFAG_IOP.getKode(), FPREFAG_IOP);
        TILGJENGELIGE.put(FPSNDDM_OP.getKode(), FPSNDDM_OP);
        TILGJENGELIGE.put(FPSNDFI.getKode(), FPSNDFI);
        TILGJENGELIGE.put(FPSNDJB_OP.getKode(), FPSNDJB_OP);
        TILGJENGELIGE.put(FPSND_OP.getKode(), FPSND_OP);
        TILGJENGELIGE.put(FSKTSKAT.getKode(), FSKTSKAT);
        TILGJENGELIGE.put(KL_KODE_FEIL_KORTTID.getKode(), KL_KODE_FEIL_KORTTID);
        TILGJENGELIGE.put(TBMOTOBS.getKode(), TBMOTOBS);
        TILGJENGELIGE.put(SPSND100D1DAGPFI.getKode(), SPSND100D1DAGPFI);
        TILGJENGELIGE.put(SPSND100D1DTRPFI.getKode(), SPSND100D1DTRPFI);
    }

    KlasseKode() {
        // Hibernate
    }

    public KlasseKode(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static KlasseKode fraKode(String kode) {
        if (TILGJENGELIGE.containsKey(kode)) {
            return TILGJENGELIGE.get(kode);
        }
        LOGGER.debug("Mottok ukjent klassekode: {}", kode);
        return UDEFINERT;
    }

}
