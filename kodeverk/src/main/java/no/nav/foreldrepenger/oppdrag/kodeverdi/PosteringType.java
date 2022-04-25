package no.nav.foreldrepenger.oppdrag.kodeverdi;

public enum PosteringType {
    YTEL, //ytelse
    FEIL, //feilutbetaling
    SKAT, //froskudsskatt
    JUST, //justering
    ;

    public static PosteringType getOrNull(String kode) {
        if (kode == null) {
            return null;
        }
        try {
            return PosteringType.valueOf(kode);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
