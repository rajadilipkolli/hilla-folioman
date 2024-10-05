package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.UploadedSchemesList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
public class NewlyInsertedSchemesListener {

    private static final Logger log = LoggerFactory.getLogger(NewlyInsertedSchemesListener.class);

    private final MFNavService mfNavService;
    private final TaskExecutor taskExecutor;

    public NewlyInsertedSchemesListener(
            MFNavService mfNavService, @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.mfNavService = mfNavService;
        this.taskExecutor = taskExecutor;
    }

    @ApplicationModuleListener
    void onOrderResponseEvent(UploadedSchemesList uploadedSchemesList) {
        log.info("Received Event :{}", uploadedSchemesList);
        List<CompletableFuture<Void>> completableFutureList = uploadedSchemesList.schemesList().stream()
                .map(schemeId -> CompletableFuture.runAsync(() -> mfNavService.getNav(schemeId), taskExecutor))
                .toList();

        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0]))
                .join();
    }
}
