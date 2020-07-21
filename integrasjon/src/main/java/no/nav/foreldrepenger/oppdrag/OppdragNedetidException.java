package no.nav.foreldrepenger.oppdrag;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class OppdragNedetidException extends IntegrasjonException {
    public OppdragNedetidException(Feil feil) {
        super(feil);
    }

    public void log(Logger logger) {
        Level logLevel = getFeil().getLogLevel();
        if (logLevel == Level.INFO) {
            String text = getFeil().toLogString();
            logger.info(text, this);
        } else {
            super.log(logger);
        }
    }

}
