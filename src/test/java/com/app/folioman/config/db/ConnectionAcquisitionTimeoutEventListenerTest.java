package com.app.folioman.config.db;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vladmihalcea.flexypool.event.ConnectionAcquisitionTimeoutEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class ConnectionAcquisitionTimeoutEventListenerTest {

    @Mock
    private ConnectionAcquisitionTimeoutEvent event;

    private ConnectionAcquisitionTimeoutEventListener listener;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        listener = new ConnectionAcquisitionTimeoutEventListener();

        Logger logger = (Logger) LoggerFactory.getLogger(ConnectionAcquisitionTimeoutEventListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.WARN);
    }

    @Test
    void constructor_ShouldInitializeWithCorrectEventClass() {
        ConnectionAcquisitionTimeoutEventListener newListener = new ConnectionAcquisitionTimeoutEventListener();

        assertNotNull(newListener);
    }

    @Test
    void on_WithValidEvent_ShouldLogWarningMessage() {
        String poolName = "testPool";
        when(event.getUniqueName()).thenReturn(poolName);

        listener.on(event);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains(poolName));
        assertTrue(loggingEvent.getFormattedMessage().contains("Connection acquisition timeout occurred"));
        assertTrue(loggingEvent.getFormattedMessage().contains("connection pool saturation"));
    }

    @Test
    void on_WithEventHavingNullUniqueName_ShouldLogWarningWithNull() {
        when(event.getUniqueName()).thenReturn(null);

        listener.on(event);

        assertEquals(1, listAppender.list.size());
        ILoggingEvent loggingEvent = listAppender.list.get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains("null"));
    }

    @Test
    void on_WithNullEvent_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> listener.on(null));
    }

    @Test
    void logger_ShouldBeStaticAndNotNull() {
        assertNotNull(ConnectionAcquisitionTimeoutEventListener.LOGGER);
        assertEquals(
                "com.app.folioman.config.db.ConnectionAcquisitionTimeoutEventListener",
                ConnectionAcquisitionTimeoutEventListener.LOGGER.getName());
    }
}
