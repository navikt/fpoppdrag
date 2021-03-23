package no.nav.foreldrepenger.oppdrag.feed.poller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import no.nav.foreldrepenger.oppdrag.dbstoette.EntityManagerAwareTest;
import no.nav.vedtak.log.util.MemoryAppender;

@Execution(ExecutionMode.SAME_THREAD)
public class FeedPollerManagerTest extends EntityManagerAwareTest {

    private static MemoryAppender logSniffer;
    private FeedPollerManager manager;

    @BeforeEach
    public void setUp() {
        logSniffer = MemoryAppender.sniff(FeedPollerManager.class);
        @SuppressWarnings("unchecked")
        Instance<FeedPoller> feedPollers = mock(Instance.class);
        @SuppressWarnings("unchecked")
        Iterator<FeedPoller> iterator = mock(Iterator.class);

        lenient().when(feedPollers.get()).thenReturn(new TestFeedPoller());
        lenient().when(feedPollers.iterator()).thenReturn(iterator);
        lenient().when(iterator.hasNext()).thenReturn(true, false);
        lenient().when(iterator.next()).thenReturn(new TestFeedPoller());
        manager = new FeedPollerManager(getEntityManager(), feedPollers);
    }

    @AfterEach
    public void tearDown() {
        logSniffer.reset();
    }

    @Test
    public void skal_legge_til_poller() {
        manager.start();
        assertThat(logSniffer.countEntries("Created thread for feed polling FeedPollerManager-UnitTestPoller-poller")).isEqualTo(1);
    }

    private static class TestFeedPoller implements FeedPoller {

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
