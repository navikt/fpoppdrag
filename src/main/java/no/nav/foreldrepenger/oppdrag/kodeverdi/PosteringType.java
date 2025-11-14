package no.nav.foreldrepenger.oppdrag.kodeverdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PosteringType {
    /** ytelse **/
    YTEL,
    /** feilutbetaling **/
    FEIL,
    /** froskudsskatt **/
    SKAT,
    /** justering **/
    JUST,
    MOTP,
    TREK,
    ;

    private static final Logger LOG = LoggerFactory.getLogger(PosteringType.class);

    public static PosteringType getOrNull(String kode) {
        if (kode == null) {
            return null;
        }
        try {
            return PosteringType.valueOf(kode);
        } catch (IllegalArgumentException ex) {
            LOG.info("Finner ikke posteringType for {}", kode);
            return null;
        }
    }
}
