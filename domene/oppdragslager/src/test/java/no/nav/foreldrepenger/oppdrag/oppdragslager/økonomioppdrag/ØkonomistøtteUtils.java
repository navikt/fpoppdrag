package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class ØkonomistøtteUtils {

    private ØkonomistøtteUtils() {
        // skjul public constructor
    }

    /**
     * Formats the given LocalDateTime into a String with the given pattern yyyy-MM-dd-HH.mm.ss.SSS
     *
     * @param dt - the object to transform
     * @return a formated string.
     */
    static String tilSpesialkodetDatoOgKlokkeslett(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        String pattern = "yyyy-MM-dd-HH.mm.ss.SSS";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return dt.format(dtf);
    }
}
