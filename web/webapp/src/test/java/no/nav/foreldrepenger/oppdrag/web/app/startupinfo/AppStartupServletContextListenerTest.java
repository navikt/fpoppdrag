package no.nav.foreldrepenger.oppdrag.web.app.startupinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.ServletContextEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import no.nav.vedtak.log.util.MemoryAppender;

public class AppStartupServletContextListenerTest {

    private AppStartupServletContextListener listener; // objekter vi tester

    private AppStartupInfoLogger mockAppStartupInfoLogger;

    public final MemoryAppender logSniffer = MemoryAppender.sniff(AppStartupServletContextListener.class);

    @BeforeEach
    public void setup() {
        listener = new AppStartupServletContextListener();
        mockAppStartupInfoLogger = mock(AppStartupInfoLogger.class);
        listener.setAppStartupInfoLogger(mockAppStartupInfoLogger);
    }

    @Test
    public void test_contextInitialized_ok() {
        listener.contextInitialized(mock(ServletContextEvent.class));

        verify(mockAppStartupInfoLogger).logAppStartupInfo();
    }

    @Test
    public void test_contextInitialized_exception() {
        doThrow(new RuntimeException("!")).when(mockAppStartupInfoLogger).logAppStartupInfo();

        listener.contextInitialized(mock(ServletContextEvent.class));

        assertThat(logSniffer.contains("FPO-753407", Level.ERROR)).isTrue();
    }

    @Test
    public void test_contextDestroyed_ok() {
        listener.contextDestroyed(mock(ServletContextEvent.class));
    }
}
