package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional(readOnly = true)
public class MfFundSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfFundSchemeService.class);

    private final MfFundSchemeRepository mfFundSchemeRepository;
    private final TransactionTemplate transactionTemplate;

    MfFundSchemeService(MfFundSchemeRepository mfFundSchemeRepository, PlatformTransactionManager transactionManager) {
        this.mfFundSchemeRepository = mfFundSchemeRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /**
     * Save a batch of mutual fund schemes with automatic transaction management.
     * If a transaction fails, it will be retried with smaller batches.
     *
     * @param mfFundSchemes List of mutual fund schemes to save
     * @param batchSize Size of each batch to process
     * @return Number of schemes successfully saved
     */
    public int saveDataInBatches(List<MfFundScheme> mfFundSchemes, int batchSize) {
        if (mfFundSchemes == null || mfFundSchemes.isEmpty()) {
            return 0;
        }

        final AtomicInteger successCount = new AtomicInteger(0);

        int totalSize = mfFundSchemes.size();

        // Process in batches
        for (int startIdx = 0; startIdx < totalSize; startIdx += batchSize) {
            final int batchStartIdx = startIdx;
            final int batchEndIdx = Math.min(startIdx + batchSize, totalSize);
            final int currentBatchSize = batchEndIdx - batchStartIdx;

            try {
                // Execute each batch in a separate transaction
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        try {
                            // Create a batch list limited to the current batch size
                            List<MfFundScheme> batch = new ArrayList<>(currentBatchSize);

                            for (int i = batchStartIdx; i < batchEndIdx; i++) {
                                batch.add(mfFundSchemes.get(i));
                            }

                            mfFundSchemeRepository.saveAll(batch);
                            successCount.addAndGet(batch.size());

                            LOGGER.debug(
                                    "Successfully saved batch of {} schemes ({}-{})",
                                    batch.size(),
                                    batchStartIdx,
                                    batchEndIdx - 1);
                        } catch (Exception e) {
                            LOGGER.error(
                                    "Error saving batch {}-{}: {}", batchStartIdx, batchEndIdx - 1, e.getMessage());
                            status.setRollbackOnly();
                            throw e;
                        }
                    }
                });
            } catch (DataAccessException e) {
                LOGGER.warn("Failed to save batch as a whole, will attempt individual saves");

                // If batch save fails, try individual saves
                for (int i = batchStartIdx; i < batchEndIdx; i++) {
                    final MfFundScheme scheme = mfFundSchemes.get(i);

                    try {
                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                            @Override
                            protected void doInTransactionWithoutResult(TransactionStatus status) {
                                try {
                                    mfFundSchemeRepository.save(scheme);
                                    successCount.incrementAndGet();
                                } catch (Exception ex) {
                                    LOGGER.debug("Could not save individual scheme: {}", ex.getMessage());
                                    status.setRollbackOnly();
                                }
                            }
                        });
                    } catch (Exception ex) {
                        LOGGER.debug("Failed to save scheme: {}", ex.getMessage());
                    }
                }
            }
        }

        return successCount.get();
    }

    public long getTotalCount() {
        return mfFundSchemeRepository.count();
    }

    public List<String> findDistinctAmfiCode() {
        return mfFundSchemeRepository.findDistinctAmfiCode();
    }
}
