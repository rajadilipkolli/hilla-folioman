package com.app.folioman.shared.events;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationEventTest {

    @Mock
    private Object mockSource;

    private TestApplicationEvent applicationEvent;

    @BeforeEach
    void setUp() {
        applicationEvent = new TestApplicationEvent(mockSource);
    }

    @Test
    void constructor_ShouldInitializeAllFields() {
        TestApplicationEvent event = new TestApplicationEvent(mockSource);

        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertSame(mockSource, event.getSource());
    }

    @Test
    void constructor_WithNullSource_ShouldAcceptNullSource() {
        TestApplicationEvent event = new TestApplicationEvent(null);

        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertNull(event.getSource());
    }

    @Test
    void getEventId_ShouldReturnUniqueUuidString() {
        TestApplicationEvent event1 = new TestApplicationEvent(mockSource);
        TestApplicationEvent event2 = new TestApplicationEvent(mockSource);

        String eventId1 = event1.getEventId();
        String eventId2 = event2.getEventId();

        assertNotNull(eventId1);
        assertNotNull(eventId2);
        assertNotEquals(eventId1, eventId2);
        assertTrue(eventId1.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void getEventId_ShouldReturnConsistentValue() {
        String eventId1 = applicationEvent.getEventId();
        String eventId2 = applicationEvent.getEventId();

        assertEquals(eventId1, eventId2);
    }

    @Test
    void getTimestamp_ShouldReturnReasonableTimestamp() {
        Instant before = Instant.now().minusSeconds(1);
        TestApplicationEvent event = new TestApplicationEvent(mockSource);
        Instant after = Instant.now().plusSeconds(1);

        Instant timestamp = event.getTimestamp();

        assertNotNull(timestamp);
        assertTrue(timestamp.isAfter(before) || timestamp.equals(before));
        assertTrue(timestamp.isBefore(after) || timestamp.equals(after));
    }

    @Test
    void getTimestamp_ShouldReturnConsistentValue() {
        Instant timestamp1 = applicationEvent.getTimestamp();
        Instant timestamp2 = applicationEvent.getTimestamp();

        assertEquals(timestamp1, timestamp2);
    }

    @Test
    void getSource_ShouldReturnProvidedSource() {
        Object source = applicationEvent.getSource();

        assertSame(mockSource, source);
    }

    @Test
    void multipleInstances_ShouldHaveDifferentEventIdsAndTimestamps() {
        TestApplicationEvent event1 = new TestApplicationEvent(mockSource);

        TestApplicationEvent event2 = new TestApplicationEvent(mockSource);

        assertNotEquals(event1.getEventId(), event2.getEventId());
        assertFalse(event2.getTimestamp().isBefore(event1.getTimestamp()));
    }

    private static class TestApplicationEvent extends ApplicationEvent {
        public TestApplicationEvent(Object source) {
            super(source);
        }
    }
}
