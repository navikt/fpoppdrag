package no.nav.foreldrepenger.oppdrag.oppdragslager.økonomioppdrag;

public enum ØkonomiUtbetFrekvens {
    DAG("DAG"),
    UKE("UKE"),
    MÅNED("MND"), //$NON-NLS-1$ //NOSONAR
    DAGER14("14DG"),
    ENGANG("ENG");

    private String utbetFrekvens;

    ØkonomiUtbetFrekvens(String utbetFrekvens) {
        this.utbetFrekvens = utbetFrekvens;
    }

    public String getUtbetFrekvens() {
        return utbetFrekvens;
    }
}
