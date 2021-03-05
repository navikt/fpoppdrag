package no.nav.foreldrepenger.oppdrag;

import no.nav.vedtak.exception.IntegrasjonException;

public class OppdragNedetidException extends IntegrasjonException {
    public OppdragNedetidException(String kode, String melding, Throwable cause) {
        super(kode, melding, cause);
    }

}
