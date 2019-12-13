package no.nav.foreldrepenger.oppdrag.feed.poller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.assertj.core.api.Assertions;
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
public class FeedPollerManagerTest {

    @Rule
    public LogSniffer logSniffer = new LogSniffer(Level.ALL);

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private FeedPollerManager manager;

    @Before
    public void setUp() {
        @SuppressWarnings("unchecked")
        Instance<FeedPoller> feedPollers = mock(Instance.class);
        @SuppressWarnings("unchecked")
        Iterator<FeedPoller> iterator = mock(Iterator.class);

        when(feedPollers.get()).thenReturn(new TestFeedPoller());
        when(feedPollers.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(new TestFeedPoller());
        manager = new FeedPollerManager(repoRule.getEntityManager(), feedPollers);
    }

    @After
    public void tearDown() {
        logSniffer.clearLog();
    }

    @Test
    public void skal_legge_til_poller() {
        manager.start();
        logSniffer.assertHasInfoMessage("Created thread for feed polling FeedPollerManager-UnitTestPoller-poller");
        Assertions.assertThat(logSniffer.countEntries("Lagt til ny poller til pollingtjeneste. poller=UnitTestPoller, delayBetweenPollingMillis=50")).isEqualTo(1);
    }

    private class TestFeedPoller implements FeedPoller {

        @Override
        public String getName() {
            return "UnitTestPoller";
        }

        @Override
        public PostTransactionHandler poll() {
            return () -> {
            };
        }
    }
}