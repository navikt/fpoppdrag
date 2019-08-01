package no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse;

import java.util.Optional;

public interface TilkjentYtelseRepository {

    Optional<TilkjentYtelseEntitet> hentTilkjentYtelse(long behandlingId);

    void lagre(TilkjentYtelseEntitet tilkjentYtelse);
}
