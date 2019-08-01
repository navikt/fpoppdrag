package no.nav.foreldrepenger.oppdrag.iverksett;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.tilkjentytelse.TilkjentYtelse;
import no.nav.foreldrepenger.kontrakter.tilkjentytelse.v1.TilkjentYtelseV1;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseEntitet;
import no.nav.foreldrepenger.oppdrag.oppdragslager.tilkjentytelse.TilkjentYtelseRepository;

@ApplicationScoped
public class TilkjentYtelseTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(TilkjentYtelseTjeneste.class);

    private TilkjentYtelseRepository tilkjentYtelseRepository;
    private TilkjentYtelseRestKlient restClient;

    TilkjentYtelseTjeneste() {
        // for CDI proxy
    }

    @Inject
    public TilkjentYtelseTjeneste(TilkjentYtelseRepository tilkjentYtelseRepository, TilkjentYtelseRestKlient restClient) {
        this.tilkjentYtelseRepository = tilkjentYtelseRepository;
        this.restClient = restClient;
    }

    public void hentOgLagreTilkjentYtelse(long behandlingId) {
        Optional<TilkjentYtelseEntitet> tilkjentYtelseEntitet = tilkjentYtelseRepository.hentTilkjentYtelse(behandlingId);
        if (tilkjentYtelseEntitet.isPresent()) {
            logger.info("Har allerede hentet og lagret tilkjent ytelse for behandlingId={}. Ignorerer.", behandlingId);
            return;
        }
        TilkjentYtelse tilkjentYtelse = restClient.hentTilkjentYtelse(behandlingId);
        tilkjentYtelseRepository.lagre(mapTilEntiteter(tilkjentYtelse, behandlingId));
    }

    private TilkjentYtelseEntitet mapTilEntiteter(TilkjentYtelse tilkjentYtelse, long behandlingId) {
        Objects.requireNonNull(tilkjentYtelse, "Tilkjent ytelse er null for behandlingId=" + behandlingId);
        if (tilkjentYtelse instanceof TilkjentYtelseV1) {
            return TilkjentYtelseV1Mapper.mapTilEntiteter((TilkjentYtelseV1) tilkjentYtelse);
        } else {
            throw new IllegalArgumentException("Utvikler/deploy-feil: ikke-støttet versjon av tilkjent ytelse: " + tilkjentYtelse.getClass().getName());
        }
    }


}
