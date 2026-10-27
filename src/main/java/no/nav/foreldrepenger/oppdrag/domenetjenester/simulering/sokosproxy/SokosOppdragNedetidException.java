package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering.sokosproxy;

import java.net.HttpURLConnection;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.VLLogLevel;

public class SokosOppdragNedetidException extends IntegrasjonException {

    public SokosOppdragNedetidException() {
        super("FPO-273197", "Oppdragssystemet er utilgjengelig via Sokos");
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
