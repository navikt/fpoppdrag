package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.fpwsproxy;

import java.net.HttpURLConnection;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.VLLogLevel;

public class OppdragNedetidException extends IntegrasjonException {

    private static final String MELDING = "Kallet mot oppdragsystemet feilet. Feilmelding og tidspunktet tilsier at oppdragsystemet har forventet nedetid (utenfor åpningstid).";

    public OppdragNedetidException() {
        super("FPO-273196", MELDING);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_UNAVAILABLE;
    }

    @Override
    public String getFeilkode() {
        return "OPPDRAG_FORVENTET_NEDETID";
    }

    @Override
    public VLLogLevel getLogLevel() {
        return VLLogLevel.NOLOG;
    }


}
