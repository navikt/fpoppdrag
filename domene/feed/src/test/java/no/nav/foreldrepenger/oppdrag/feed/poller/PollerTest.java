package no.nav.foreldrepenger.oppdrag.feed.poller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.log.util.MemoryAppender;

public class PollerTest {

    public MemoryAppender logSniffer = MemoryAppender.sniff(Poller.class);

    private Poller poller;
    private FeedPoller feedPoller;

    @BeforeEach
    public void setUp() {
        feedPoller = mock(FeedPoller.class);
        when(feedPoller.getName()).thenReturn("UnitTestPoller");
        poller = new Poller();
    }

    @AfterEach
    public void tearDown() {
        logSniffer.reset();
    }

    @Test
    public void skal_logge_exception_ved_feil_ved_polling() {
        Poller pollerSomFårNPE = new Poller(null, null);

        pollerSomFårNPE.run();
        assertThat(logSniffer.countEntries("FPO-852160")).isEqualTo(1);
    }

    @Test
    public void skal_behandle_ukjent_feil() {
        doThrow(new RuntimeException()).when(feedPoller).poll();
        poller.run();

        assertThat(logSniffer.countEntries("FPO-852160")).isEqualTo(1);
    }
}
