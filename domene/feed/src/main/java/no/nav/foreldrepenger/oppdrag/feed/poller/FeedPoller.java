package no.nav.foreldrepenger.oppdrag.feed.poller;

public interface FeedPoller {

    String getName();

    PostTransactionHandler poll();
}
