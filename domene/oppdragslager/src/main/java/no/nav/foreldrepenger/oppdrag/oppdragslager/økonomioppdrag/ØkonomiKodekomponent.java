package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

public enum ØkonomiKodekomponent {
    VLFP("VLFP"),
    OS("OS");

    private String kodekomponent;

    ØkonomiKodekomponent(String kodekomponent) {
        this.kodekomponent = kodekomponent;
    }

    public String getKodekomponent() {
        return kodekomponent;
    }
}
