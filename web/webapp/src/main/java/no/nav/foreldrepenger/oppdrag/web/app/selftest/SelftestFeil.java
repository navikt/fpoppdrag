package no.nav.foreldrepenger.oppdrag.web.app.selftest;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.io.IOException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface SelftestFeil extends DeklarerteFeil {

    SelftestFeil FACTORY = FeilFactory.create(SelftestFeil.class);

    @TekniskFeil(feilkode = "FPO-635121", feilmelding = "Klarte ikke å lese build time properties fil", logLevel = LogLevel.ERROR)
    Feil klarteIkkeÅLeseBuildTimePropertiesFil(IOException e);

    @TekniskFeil(feilkode = "FPO-287026", feilmelding = "Dupliserte selftest navn %s", logLevel = WARN)
    Feil dupliserteSelftestNavn(String name);

    @TekniskFeil(feilkode = "FPO-409676", feilmelding = "Uventet feil", logLevel = ERROR)
    Feil uventetSelftestFeil(IOException e);

    @TekniskFeil(feilkode = "FPO-932415", feilmelding = "Selftest ERROR: %s. Endpoint: %s. Responstid: %s. Feilmelding: %s.", logLevel = ERROR)
    Feil kritiskSelftestFeilet(String description, String endpoint, String responseTime, String message);

    @TekniskFeil(feilkode = "FPO-984256", feilmelding = "Selftest ERROR: %s. Endpoint: %s. Responstid: %s. Feilmelding: %s.", logLevel = WARN)
    Feil ikkeKritiskSelftestFeilet(String description, String endpoint, String responsTime, String message);
}
