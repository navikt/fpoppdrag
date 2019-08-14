package no.nav.foreldrepenger.oppdrag.web.server.jetty.abac;

import java.util.Collection;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface PdpRequestBuilderFeil extends DeklarerteFeil {

    PdpRequestBuilderFeil FACTORY = FeilFactory.create(PdpRequestBuilderFeil.class);

    @TekniskFeil(feilkode = "FPO-49016", feilmelding = "Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", logLevel = LogLevel.WARN)
    Feil ugyldigInputFlereBehandlingIder(Collection<?> behandlingId);

}
