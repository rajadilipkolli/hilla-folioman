package com.app.folioman.config.db;

import com.vladmihalcea.flexypool.event.ConnectionAcquisitionTimeoutEvent;
import com.vladmihalcea.flexypool.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionAcquisitionTimeoutEventListener extends EventListener<ConnectionAcquisitionTimeoutEvent> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectionAcquisitionTimeoutEventListener.class);

    public ConnectionAcquisitionTimeoutEventListener() {
        super(ConnectionAcquisitionTimeoutEvent.class);
    }

    @Override
    public void on(ConnectionAcquisitionTimeoutEvent event) {
        LOGGER.info("Caught ConnectionAcquisitionTimeoutEvent {}", event.getUniqueName());
    }
}
