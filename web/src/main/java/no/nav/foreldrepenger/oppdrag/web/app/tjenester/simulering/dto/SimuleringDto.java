package no.nav.foreldrepenger.oppdrag.web.app.tjenester.simulering.dto;

public class SimuleringDto {

    private DetaljertSimuleringResultatDto simuleringResultat;
    private DetaljertSimuleringResultatDto simuleringResultatUtenInntrekk;
    private boolean slåttAvInntrekk;

    public SimuleringDto(DetaljertSimuleringResultatDto simuleringResultat, boolean slåttAvInntrekk) {
        this.simuleringResultat = simuleringResultat;
        this.slåttAvInntrekk = slåttAvInntrekk;
    }

    public SimuleringDto(DetaljertSimuleringResultatDto simuleringResultat, DetaljertSimuleringResultatDto simuleringResultatUtenInntrekk) {
        this.simuleringResultat = simuleringResultat;
        this.simuleringResultatUtenInntrekk = simuleringResultatUtenInntrekk;
    }

    public DetaljertSimuleringResultatDto getSimuleringResultat() {
        return simuleringResultat;
    }

    public DetaljertSimuleringResultatDto getSimuleringResultatUtenInntrekk() {
        return simuleringResultatUtenInntrekk;
    }

    public boolean isSlåttAvInntrekk() {
        return slåttAvInntrekk;
    }
}
