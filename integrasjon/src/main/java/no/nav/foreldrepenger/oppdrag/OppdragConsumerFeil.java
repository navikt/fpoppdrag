package no.nav.foreldrepenger.oppdrag;

import javax.xml.ws.WebServiceException;

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
    Feil oppdragsystemetHarNedetid(WebServiceException cause);

    @IntegrasjonFeil(feilkode = "FPO-852145", feilmelding = "Simulering feilet. Fikk uventet feil mot oppdragssytemet", logLevel = LogLevel.WARN)
    Feil feilUnderKallTilSimuleringtjenesten(WebServiceException e);

    @IntegrasjonFeil(feilkode = "FPO-845125", feilmelding = "Simulering feilet. Mottok feilmelding fra oppdragsystemet: source='%s' type='%s' message='%s' rootcause='%s' timestamp='%s'", logLevel = LogLevel.WARN)
    Feil feilUnderBehandlingAvSimulering(String source, String errorType, String errorMessage, String rootCause, String dateTimeStamp, Exception e);

}


