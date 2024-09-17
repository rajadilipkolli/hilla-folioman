package com.app.folioman.mfschemes.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class NewlyInsertedSchemesListener {

    private static final Logger log = LoggerFactory.getLogger(NewlyInsertedSchemesListener.class);

    @ApplicationModuleListener
    void onOrderResponseEvent(List<String> schemes) {
        log.info("Received Event :{}", schemes);
    }
}
