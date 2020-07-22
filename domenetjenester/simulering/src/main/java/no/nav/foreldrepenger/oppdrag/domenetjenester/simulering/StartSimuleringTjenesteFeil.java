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

    @TekniskFeil(feilkode = "FPO-852146", feilmelding = "Utvikler-feil: Mangler mapping mellom fagområdekode og ytelsetype for behandlingId=%s fagområdekode=%s", logLevel = WARN)
    Feil manglerMappingMellomFagområdeKodeOgYtleseType(Long behandlingId, List<String> fagområdeKode);

    @TekniskFeil(feilkode = "FPO-810466", feilmelding = "Utvikler-feil: Klarer ikke utlede unik ytelsetype for behandlingId=%s fagområdekode=%s", logLevel = WARN)
    Feil ikkeUnikYtelseType(Long behandlingId, List<String> fagområdeKode);

    @TekniskFeil(feilkode = "FPO-811943", feilmelding = "Manglet fagsystemId i mottat respons for behandlingId=%s periode=%s-%s", logLevel = WARN)
    Feil mangletFagsystemId(Long behandlingId, String periodeFom, String periodeTom);

}