package com.app.folioman.shared.events;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getSource()).isSameAs(mockSource);
    }

    @Test
    void constructor_WithNullSource_ShouldAcceptNullSource() {
        TestApplicationEvent event = new TestApplicationEvent(null);

        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getSource()).isNull();
    }

    @Test
    void getEventId_ShouldReturnUniqueUuidString() {
        TestApplicationEvent event1 = new TestApplicationEvent(mockSource);
        TestApplicationEvent event2 = new TestApplicationEvent(mockSource);

        String eventId1 = event1.getEventId();
        String eventId2 = event2.getEventId();

        assertThat(eventId1).isNotNull();
        assertThat(eventId2).isNotEqualTo(eventId1);
        assertThat(eventId1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void getEventId_ShouldReturnConsistentValue() {
        String eventId1 = applicationEvent.getEventId();
        String eventId2 = applicationEvent.getEventId();

        assertThat(eventId2).isEqualTo(eventId1);
    }

    @Test
    void getTimestamp_ShouldReturnReasonableTimestamp() {
        Instant before = Instant.now().minusSeconds(1);
        TestApplicationEvent event = new TestApplicationEvent(mockSource);
        Instant after = Instant.now().plusSeconds(1);

        Instant timestamp = event.getTimestamp();

        assertThat(timestamp).isNotNull();
        assertThat(timestamp.isAfter(before) || timestamp.equals(before)).isTrue();
        assertThat(timestamp.isBefore(after) || timestamp.equals(after)).isTrue();
    }

    @Test
    void getTimestamp_ShouldReturnConsistentValue() {
        Instant timestamp1 = applicationEvent.getTimestamp();
        Instant timestamp2 = applicationEvent.getTimestamp();

        assertThat(timestamp2).isEqualTo(timestamp1);
    }

    @Test
    void getSource_ShouldReturnProvidedSource() {
        Object source = applicationEvent.getSource();

        assertThat(source).isSameAs(mockSource);
    }

    @Test
    void multipleInstances_ShouldHaveDifferentEventIdsAndTimestamps() {
        TestApplicationEvent event1 = new TestApplicationEvent(mockSource);

        TestApplicationEvent event2 = new TestApplicationEvent(mockSource);

        assertThat(event2.getEventId()).isNotEqualTo(event1.getEventId());
        assertThat(event2.getTimestamp()).isAfterOrEqualTo(event1.getTimestamp());
    }

    private static class TestApplicationEvent extends ApplicationEvent {
        public TestApplicationEvent(Object source) {
            super(source);
        }
    }
}
