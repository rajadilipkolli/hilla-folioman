package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.UploadedSchemesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
public class NewlyInsertedSchemesListener {

    private static final Logger log = LoggerFactory.getLogger(NewlyInsertedSchemesListener.class);

    private final MFNavService mfNavService;

    public NewlyInsertedSchemesListener(MFNavService mfNavService) {
        this.mfNavService = mfNavService;
    }

    @ApplicationModuleListener
    void onOrderResponseEvent(UploadedSchemesList uploadedSchemesList) {
        log.info("Received Event :{}", uploadedSchemesList);
        // Delegate async processing to the service layer to avoid transactional issues
        mfNavService.processNavsAsync(uploadedSchemesList.schemesList());
    }
}
