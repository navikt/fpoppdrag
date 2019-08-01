package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

public enum YtelseType {
    ENGANGSTØNAD("ES"), //$NON-NLS-1$ //NOSONAR
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP");

    private String kode;

    YtelseType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
