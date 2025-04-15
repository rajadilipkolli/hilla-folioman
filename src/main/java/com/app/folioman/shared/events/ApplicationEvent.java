package com.app.folioman.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all application events.
 * Provides common properties for tracking events across modules.
 */
public abstract class ApplicationEvent {

    private final String eventId;
    private final Instant timestamp;
    private final Object source;

    protected ApplicationEvent(Object source) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.source = source;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Object getSource() {
        return source;
    }
}
