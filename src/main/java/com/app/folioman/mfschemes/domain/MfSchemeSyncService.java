package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.config.MfSchemesProperties;
import com.app.folioman.mfschemes.exception.MutualFundDataException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MfSchemeSyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeSyncService.class);
    private static final String ISIN_KEY = BSEStarMasterDataService.AMFI_ISIN_KEY;

    private final BSEStarMasterDataService bseStarMasterDataService;
    private final AmfiService amfiService;
    private final MfFundSchemeService mfFundSchemeService;
    private final MfFundSchemeRepository mfFundSchemeRepository;
    private final MfSchemesProperties properties;

    MfSchemeSyncService(
            BSEStarMasterDataService bseStarMasterDataService,
            AmfiService amfiService,
            MfFundSchemeService mfFundSchemeService,
            MfFundSchemeRepository mfFundSchemeRepository,
            MfSchemesProperties properties) {
        this.bseStarMasterDataService = bseStarMasterDataService;
        this.amfiService = amfiService;
        this.mfFundSchemeService = mfFundSchemeService;
        this.mfFundSchemeRepository = mfFundSchemeRepository;
        this.properties = properties;
    }

    public record SyncStatistics(
            AtomicInteger newSchemes,
            AtomicInteger updatedSchemes,
            AtomicInteger skippedSchemes,
            AtomicInteger failedSchemes) {
        public SyncStatistics() {
            this(new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0), new AtomicInteger(0));
        }
    }

    @Job(name = "Update MF Schemes", retries = 3)
    public void syncAllSchemes() {
        LOGGER.info("Starting MF Scheme Synchronization...");
        SyncStatistics stats = new SyncStatistics();

        try {
            String bseMasterData = bseStarMasterDataService.downloadBseMasterData();
            if (bseMasterData == null) {
                LOGGER.warn("BSE Master Data download returned null. Aborting sync.");
                return;
            }

            BSEStarMasterDataService.BseMasterDataResult bseDataResult =
                    bseStarMasterDataService.parseBseMasterData(bseMasterData);

            amfiService.fetchAmfiSchemeData(amfiDataMap -> {
                if (amfiDataMap.isEmpty()) return;

                try {
                    Map<String, String> amfiCodeIsinMapping = getAmfiCodeISINMapping(amfiDataMap);
                    Map<String, MfFundSchemeEntity> incomingSchemesMap =
                            bseStarMasterDataService.processAmfiBatch(bseDataResult, amfiDataMap, amfiCodeIsinMapping);

                    if (!incomingSchemesMap.isEmpty()) {
                        processIncomingBatch(incomingSchemesMap, stats);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error processing AMFI batch", e);
                    stats.failedSchemes().incrementAndGet();
                }
            });

            LOGGER.info(
                    "MF Scheme Sync completed. New: {}, Updated: {}, Skipped: {}, Failed: {}",
                    stats.newSchemes().get(),
                    stats.updatedSchemes().get(),
                    stats.skippedSchemes().get(),
                    stats.failedSchemes().get());

        } catch (IOException | com.opencsv.exceptions.CsvException e) {
            LOGGER.error("Failed to download or parse BSE Master Data", e);
            throw new MutualFundDataException("Failed to download or parse BSE Master Data", e);
        }
    }

    private void processIncomingBatch(Map<String, MfFundSchemeEntity> incomingSchemesMap, SyncStatistics stats) {
        Set<Long> amfiCodes =
                incomingSchemesMap.keySet().stream().map(Long::valueOf).collect(Collectors.toSet());

        // Fetch existing schemes for O(1) lookup
        Map<Long, MfFundSchemeEntity> existingSchemesMap = mfFundSchemeRepository.findByAmfiCodeIn(amfiCodes).stream()
                .collect(Collectors.toMap(MfFundSchemeEntity::getAmfiCode, scheme -> scheme));

        List<MfFundSchemeEntity> newSchemesToSave = new ArrayList<>();
        List<MfFundSchemeEntity> updatedSchemesToSave = new ArrayList<>();

        for (Map.Entry<String, MfFundSchemeEntity> entry : incomingSchemesMap.entrySet()) {
            Long amfiCode = Long.valueOf(entry.getKey());
            MfFundSchemeEntity incoming = entry.getValue();
            MfFundSchemeEntity existing = existingSchemesMap.get(amfiCode);

            if (existing == null) {
                newSchemesToSave.add(incoming);
            } else if (hasSchemeChanged(existing, incoming)) {
                // Update fields
                existing.setName(incoming.getName());
                existing.setIsin(incoming.getIsin());
                existing.setStartDate(incoming.getStartDate());
                existing.setEndDate(incoming.getEndDate());
                existing.setAmc(incoming.getAmc());
                existing.setMfSchemeTypeEntity(incoming.getMfSchemeTypeEntity());
                existing.setSid(incoming.getSid());
                existing.setRta(incoming.getRta());
                existing.setPlan(incoming.getPlan());
                existing.setRtaCode(incoming.getRtaCode());
                existing.setAmcCode(incoming.getAmcCode());

                updatedSchemesToSave.add(existing);
            } else {
                stats.skippedSchemes().incrementAndGet();
            }
        }

        if (!newSchemesToSave.isEmpty()) {
            int saved = mfFundSchemeService.saveDataInBatches(newSchemesToSave, properties.getBatchSize());
            stats.newSchemes().addAndGet(saved);
        }

        if (!updatedSchemesToSave.isEmpty()) {
            int saved = mfFundSchemeService.saveDataInBatches(updatedSchemesToSave, properties.getBatchSize());
            stats.updatedSchemes().addAndGet(saved);
        }
    }

    private boolean hasSchemeChanged(MfFundSchemeEntity existing, MfFundSchemeEntity incoming) {
        if (!Objects.equals(existing.getName(), incoming.getName())) return true;
        if (!Objects.equals(existing.getIsin(), incoming.getIsin())) return true;
        if (!Objects.equals(existing.getStartDate(), incoming.getStartDate())) return true;
        if (!Objects.equals(existing.getEndDate(), incoming.getEndDate())) return true;
        if (!Objects.equals(existing.getSid(), incoming.getSid())) return true;
        if (!Objects.equals(existing.getRta(), incoming.getRta())) return true;
        if (!Objects.equals(existing.getPlan(), incoming.getPlan())) return true;
        if (!Objects.equals(existing.getRtaCode(), incoming.getRtaCode())) return true;
        if (!Objects.equals(existing.getAmcCode(), incoming.getAmcCode())) return true;

        Integer existingAmcId = existing.getAmc() != null ? existing.getAmc().getId() : null;
        Integer incomingAmcId = incoming.getAmc() != null ? incoming.getAmc().getId() : null;
        if (!Objects.equals(existingAmcId, incomingAmcId)) return true;

        Integer existingCategoryId = existing.getMfSchemeTypeEntity() != null
                ? existing.getMfSchemeTypeEntity().getSchemeTypeId()
                : null;
        Integer incomingCategoryId = incoming.getMfSchemeTypeEntity() != null
                ? incoming.getMfSchemeTypeEntity().getSchemeTypeId()
                : null;
        return !Objects.equals(existingCategoryId, incomingCategoryId);
    }

    private Map<String, String> getAmfiCodeISINMapping(Map<String, Map<String, String>> amfiDataMap) {
        Map<String, String> localAmfiCodeIsinMap = new java.util.HashMap<>();
        for (Map.Entry<String, Map<String, String>> outerEntry : amfiDataMap.entrySet()) {
            String amfiCode = outerEntry.getKey();
            String isin = outerEntry.getValue().get(ISIN_KEY);
            if (isin != null) {
                String processedIsin = (isin.length() > 12) ? isin.substring(0, 12) : isin;
                localAmfiCodeIsinMap.putIfAbsent(amfiCode, processedIsin);
            }
        }
        return localAmfiCodeIsinMap;
    }
}
