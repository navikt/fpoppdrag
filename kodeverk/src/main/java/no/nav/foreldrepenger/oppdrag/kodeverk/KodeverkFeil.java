package no.nav.foreldrepenger.oppdrag.kodeverk;

import javax.persistence.NoResultException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface KodeverkFeil extends DeklarerteFeil {

    KodeverkFeil FEILFACTORY = FeilFactory.create(KodeverkFeil.class);

    @TekniskFeil(feilkode = "FPO-314678", feilmelding = "Kan ikke finne kodeverk for type '%s', kode '%s'", logLevel = LogLevel.WARN)
    Feil kanIkkeFinneKodeverk(String klassetype, String kode, NoResultException e);

}
