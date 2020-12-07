package no.nav.foreldrepenger.oppdrag.feed.poller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import no.nav.foreldrepenger.oppdrag.dbstoette.EntityManagerAwareExtension;
import no.nav.vedtak.log.util.MemoryAppender;

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(EntityManagerAwareExtension.class)
public class FeedPollerManagerTest {

    private static MemoryAppender logSniffer;
    private FeedPollerManager manager;

    @BeforeEach
    public void setUp(EntityManager entityManager) {
        logSniffer = MemoryAppender.sniff(FeedPollerManager.class);
        @SuppressWarnings("unchecked")
        Instance<FeedPoller> feedPollers = mock(Instance.class);
        @SuppressWarnings("unchecked")
        Iterator<FeedPoller> iterator = mock(Iterator.class);

        when(feedPollers.get()).thenReturn(new TestFeedPoller());
        when(feedPollers.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(new TestFeedPoller());
        manager = new FeedPollerManager(entityManager, feedPollers);
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
