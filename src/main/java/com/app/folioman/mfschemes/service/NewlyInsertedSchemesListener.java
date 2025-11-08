package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.UploadedSchemesList;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
public class NewlyInsertedSchemesListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewlyInsertedSchemesListener.class);

    private final MFNavService mfNavService;

    public NewlyInsertedSchemesListener(MFNavService mfNavService) {
        this.mfNavService = mfNavService;
    }

    @ApplicationModuleListener
    @EventListener
    void onOrderResponseEvent(UploadedSchemesList uploadedSchemesList) {
        LOGGER.info("Received Event :{}", uploadedSchemesList);
        // Delegate async processing to the service layer to avoid transactional issues
        mfNavService.processNavsAsync(uploadedSchemesList.schemesList().stream()
                .filter(Objects::nonNull)
                .toList());
    }
}
