package com.app.folioman.config.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.vladmihalcea.flexypool.event.ConnectionAcquisitionTimeoutEvent;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(ConnectionAcquisitionTimeoutEventListener.class);
        logger.detachAppender(listAppender);
    }

    @Test
    void constructor_ShouldCreateNonNullInstance() {
        ConnectionAcquisitionTimeoutEventListener newListener = new ConnectionAcquisitionTimeoutEventListener();

        assertThat(newListener).isNotNull();
    }

    @Test
    void on_WithValidEvent_ShouldLogWarningMessage() {
        String poolName = "testPool";
        when(event.getUniqueName()).thenReturn(poolName);

        listener.on(event);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent loggingEvent = listAppender.list.getFirst();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getFormattedMessage()).contains(poolName);
        assertThat(loggingEvent.getFormattedMessage()).contains("Connection acquisition timeout occurred");
        assertThat(loggingEvent.getFormattedMessage()).contains("connection pool saturation");
        verify(event).getUniqueName();
    }

    @Test
    void on_WithEventHavingNullUniqueName_ShouldLogWarningWithNull() {
        when(event.getUniqueName()).thenReturn(null);

        listener.on(event);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent loggingEvent = listAppender.list.getFirst();
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.WARN);
        assertThat(loggingEvent.getFormattedMessage()).contains("null");
    }

    @Test
    void on_WithNullEvent_ShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> listener.on(null));
    }
}
