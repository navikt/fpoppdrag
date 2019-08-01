package no.nav.foreldrepenger.oppdrag.feed.util;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KafkaConsumerFeil extends DeklarerteFeil {

    KafkaConsumerFeil FACTORY = FeilFactory.create(KafkaConsumerFeil.class);

    @TekniskFeil(feilkode = "FPO-051", feilmelding = "Kan ikke commit offset. Meldinger kan bli lest flere ganger.", logLevel = LogLevel.WARN)
    Feil kunneIkkeCommitOffset(Class<?> exceptionClass, String exceptionMessage, Exception cause);
}
