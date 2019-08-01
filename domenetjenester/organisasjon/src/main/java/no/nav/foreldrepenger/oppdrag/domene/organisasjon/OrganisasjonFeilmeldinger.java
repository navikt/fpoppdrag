package no.nav.foreldrepenger.oppdrag.domene.organisasjon;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface OrganisasjonFeilmeldinger extends DeklarerteFeil {
    OrganisasjonFeilmeldinger FACTORY = FeilFactory.create(OrganisasjonFeilmeldinger.class);

    @TekniskFeil(feilkode = "FPO-9056718", feilmelding = "Fant ikke organisasjon", logLevel = WARN)
    Feil fantIkkeOrganisasjon(HentOrganisasjonOrganisasjonIkkeFunnet cause);

    @TekniskFeil(feilkode = "FPO-9056719", feilmelding = "Ugyldig organisasjon kode", logLevel = WARN)
    Feil ugyldigInput(HentOrganisasjonUgyldigInput cause);
}
