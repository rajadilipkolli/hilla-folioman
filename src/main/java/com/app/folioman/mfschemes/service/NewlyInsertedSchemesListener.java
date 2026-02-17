package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.UploadedSchemesList;
import java.util.List;
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

    NewlyInsertedSchemesListener(MFNavService mfNavService) {
        this.mfNavService = mfNavService;
    }

    @ApplicationModuleListener
    @EventListener
    void onOrderResponseEvent(UploadedSchemesList uploadedSchemesList) {
        LOGGER.info("Received Event :{}", uploadedSchemesList);
        // Delegate async processing to the service layer to avoid transactional issues
        List<Long> schemeCodes = uploadedSchemesList.schemesList().stream()
                .filter(Objects::nonNull)
                .toList();
        mfNavService.processNavsAsync(schemeCodes);
    }
}
