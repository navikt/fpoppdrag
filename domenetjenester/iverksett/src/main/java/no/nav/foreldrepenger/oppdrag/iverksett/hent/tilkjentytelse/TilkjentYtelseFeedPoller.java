package no.nav.foreldrepenger.oppdrag.iverksett.hent.tilkjentytelse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.codahale.metrics.annotation.Timed;

import no.nav.foreldrepenger.oppdrag.feed.poller.FeedPoller;
import no.nav.foreldrepenger.oppdrag.feed.poller.PostTransactionHandler;

@ApplicationScoped
public class TilkjentYtelseFeedPoller implements FeedPoller {

    private static final String FEED_NAME = "fp-tilkjentytelse-v1-topic";

    private TilkjentYtelseReader tilkjentYtelseReader;

    TilkjentYtelseFeedPoller() {
        // for CDI proxy
    }

    @Inject
    public TilkjentYtelseFeedPoller(TilkjentYtelseReader tilkjentYtelseReader) {
        this.tilkjentYtelseReader = tilkjentYtelseReader;
    }

    @Override
    public String getName() {
        return FEED_NAME;
    }

    @Timed
    @Override
    public PostTransactionHandler poll() {
        return tilkjentYtelseReader.hentOgBehandleMeldinger();
    }
}
