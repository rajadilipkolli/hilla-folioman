package com.app.folioman.mfschemes.bootstrap;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.AmfiService;
import com.app.folioman.mfschemes.service.BSEStarMasterDataService;
import com.app.folioman.mfschemes.service.MfFundSchemeService;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
    public static final String ISIN_KEY = "ISIN Div Payout/ ISIN GrowthISIN Div Reinvestment";

    private final AmfiService amfiService;
    private final BSEStarMasterDataService bseStarMasterDataService;
    private final MfFundSchemeService mfFundSchemeService;
    private final MFNavService mfNavService;

    public Initializer(
            AmfiService amfiService,
            BSEStarMasterDataService bseStarMasterDataService,
            MfFundSchemeService mfFundSchemeService,
            MFNavService mfNavService) {
        this.amfiService = amfiService;
        this.bseStarMasterDataService = bseStarMasterDataService;
        this.mfFundSchemeService = mfFundSchemeService;
        this.mfNavService = mfNavService;
    }

    @EventListener
    public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
        LOGGER.info("Loading all Mutual Funds on StartUp");
        try {
            Map<String, Map<String, String>> amfiDataMap = amfiService.fetchAmfiSchemeData();
            if (!amfiDataMap.isEmpty()) {
                long totalCount = mfFundSchemeService.getTotalCount();

                // Only proceed if there's more data to load
                if (amfiDataMap.size() > totalCount) {

                    Map<String, String> amfiCodeIsinMapping = getAmfiCodeISINMapping(amfiDataMap);

                    Map<String, MfFundScheme> bseStarMasterDataMap =
                            bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping);

                    // Process data
                    processMasterData(bseStarMasterDataMap, amfiDataMap.keySet());
                    LOGGER.debug("Completed loading initial data.");
                }
            }
        } catch (HttpClientErrorException | IOException httpClientErrorException) {
            LOGGER.error("Failed to load all Funds", httpClientErrorException);
        } catch (CsvException e) {
            LOGGER.error("Failed to process CSV data", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getAmfiCodeISINMapping(Map<String, Map<String, String>> amfiDataMap) {
        // Using ConcurrentHashMap to ensure thread safety
        Map<String, String> amfiCodeIsinMapping = new ConcurrentHashMap<>(mfNavService.getAmfiCodeIsinMap());

        // Only proceed if the mapping is empty
        if (amfiCodeIsinMapping.isEmpty()) {
            // Traverse the amfiService to create a map of amfiCode and ISIN
            for (Map.Entry<String, Map<String, String>> outerEntry : amfiDataMap.entrySet()) {
                String amfiCode = outerEntry.getKey(); // The AMFI code
                Map<String, String> schemeData = outerEntry.getValue(); // Inner map with scheme details

                // Retrieve ISIN value directly, avoiding redundant containsKey check
                String isin = schemeData.get(ISIN_KEY);

                // Proceed only if ISIN is not null
                if (isin != null) {
                    // Optimize the length check and substring operation
                    String processedIsin = (isin.length() > 12) ? isin.substring(0, 12) : isin;

                    // Map ISIN to AMFI code - ConcurrentHashMap's putIfAbsent is thread-safe
                    amfiCodeIsinMapping.putIfAbsent(processedIsin, amfiCode);
                }
            }
        }
        return amfiCodeIsinMapping;
    }

    /**
     * Process master data by filtering and saving new schemes.
     * This method is designed to work with both sequential and parallel streams.
     * Current implementation uses sequential stream for predictable behavior.
     * If changed to parallel in the future, ensure thread safety of all operations.
     *
     * @param bseStarMasterDataMap Map of scheme data from BSE Star
     * @param amfiCodeSet Set of AMFI codes to process
     */
    private void processMasterData(Map<String, MfFundScheme> bseStarMasterDataMap, Set<String> amfiCodeSet) {
        // Thread-safe read operation
        Set<String> distinctAmfiCodeFromDB = new HashSet<>(this.mfFundSchemeService.findDistinctAmfiCode());

        // All operations in this stream are stateless and have no side effects,
        // making it safe for potential parallel execution in the future if needed
        List<MfFundScheme> mfFundSchemeList = bseStarMasterDataMap.keySet().stream()
                // These filter operations are stateless and thread-safe
                .filter(amfiCodeSet::contains)
                .filter(s -> !distinctAmfiCodeFromDB.contains(s))
                .distinct()
                // Map operation gets values from a shared map but doesn't modify it
                .map(bseStarMasterDataMap::get)
                // Using toList() which is thread-safe and immutable
                .toList();

        // Batch insert instead of inserting individually - contained in a single transaction
        if (!mfFundSchemeList.isEmpty()) {
            mfFundSchemeService.saveData(mfFundSchemeList);
        }
    }
}
