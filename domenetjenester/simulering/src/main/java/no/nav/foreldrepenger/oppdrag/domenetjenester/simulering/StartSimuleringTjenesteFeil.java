package no.nav.foreldrepenger.oppdrag.domenetjenester.simulering;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.List;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface StartSimuleringTjenesteFeil extends DeklarerteFeil {

    StartSimuleringTjenesteFeil FACTORY = FeilFactory.create(StartSimuleringTjenesteFeil.class);

    @TekniskFeil(feilkode = "FPO-832562", feilmelding = "Kunne ikke tolke mottatt oppdrag XML", logLevel = LogLevel.WARN)
    Feil kunneIkkeUnmarshalleOppdragXml(Exception e);

    @TekniskFeil(feilkode = "FPO-852524", feilmelding = "Kunne ikke marshalle simulering request til simulering. behandlingId=%s", logLevel = LogLevel.WARN)
    Feil kunneIkkeMarshalleSimuleringRequest(Long behandlingId, Exception e);

    @TekniskFeil(feilkode = "FPO-852523", feilmelding = "Kunne ikke marshalle simuleringresultatet til XML. behandlingId=%s", logLevel = LogLevel.WARN)
    Feil kunneIkkeMarshalleSimuleringResultat(Long behandlingId, Exception e);

    @TekniskFeil(feilkode = "FPO-845125", feilmelding = "Simulering feilet. Mottok feilmelding fra oppdragsystemet: source='%s' type='%s' message='%s' rootcause='%s' timestamp='%s'", logLevel = LogLevel.WARN)
    Feil feilUnderBehandlingAvSimulering(String source, String errorType, String errorMessage, String rootCause, String dateTimeStamp, Exception e);

    @TekniskFeil(feilkode = "FPO-852145", feilmelding = "Simulering feilet. Fikk uventet feil mot oppdragssytemet", logLevel = LogLevel.WARN)
    Feil feilUnderKallTilSimuleringtjeneste(Exception e);

    @TekniskFeil(feilkode = "FPO-852146", feilmelding = "Utvikler-feil: Mangler mapping mellom fagområdekode og ytelsetype for behandlingId=%s fagområdekode=%s", logLevel = WARN)
    Feil manglerMappingMellomFagområdeKodeOgYtleseType(Long behandlingId, List<String> fagområdeKode);

}