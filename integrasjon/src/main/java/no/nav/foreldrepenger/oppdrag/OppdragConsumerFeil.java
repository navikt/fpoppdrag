package no.nav.foreldrepenger.oppdrag;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface OppdragConsumerFeil extends DeklarerteFeil {

    OppdragConsumerFeil FACTORY = FeilFactory.create(OppdragConsumerFeil.class);

    @IntegrasjonFeil(feilkode = "FPO-273196",
        feilmelding = "Kallet mot oppdragsystemet feilet. Feilmelding og tidspunktet tilsier at oppdragsystemet har forventet nedetid (utenfor åpningstid)."
        , logLevel = LogLevel.INFO
        , exceptionClass = OppdragNedetidException.class)
    Feil oppdragsystemetHarNedeteid(SOAPFaultException cause);
}


