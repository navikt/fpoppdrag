package no.nav.foreldrepenger.oppdrag.feed.poller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.qos.logback.classic.Level;
import no.nav.foreldrepenger.oppdrag.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.oppdrag.test.LogSniffer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class PollerTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.ALL);

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private Poller poller;
    private FeedPoller feedPoller;

    @Before
    public void setUp() {
        feedPoller = mock(FeedPoller.class);
        when(feedPoller.getName()).thenReturn("UnitTestPoller");
        poller = new Poller();
    }

    @After
    public void tearDown() {
        logSniffer.clearLog();
    }

    @Test
    public void skal_logge_exception_ved_feil_ved_polling() {
        Poller pollerSomFårNPE = new Poller(null, null);

        pollerSomFårNPE.run();
        logSniffer.assertHasWarnMessage("FPO-852160:Kunne ikke polle kafka hendelser, venter til neste runde(runde=1)");
    }

    @Test
    public void skal_behandle_ukjent_feil() {
        doThrow(new RuntimeException()).when(feedPoller).poll();
        poller.run();

        logSniffer.assertHasWarnMessage("FPO-852160:Kunne ikke polle kafka hendelser, venter til neste runde(runde=1)");
    }
}