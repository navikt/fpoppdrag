package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy;

import no.nav.vedtak.exception.IntegrasjonException;

public class OppdragNedetidException extends IntegrasjonException {

    private static final String MELDING = "Kallet mot oppdragsystemet feilet. Feilmelding og tidspunktet tilsier at oppdragsystemet har forventet nedetid (utenfor åpningstid).";

    public OppdragNedetidException() {
        super("FPO-273196", MELDING);
    }

}