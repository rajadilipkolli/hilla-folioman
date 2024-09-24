package com.app.folioman.mfschemes.bootstrap;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.AmfiService;
import com.app.folioman.mfschemes.service.BSEStarMasterDataService;
import com.app.folioman.mfschemes.service.MfFundSchemeService;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
    public static final String ISIN_KEY = "ISIN Div Payout/ ISIN GrowthISIN Div Reinvestment";

    private final RestClient restClient;
    private final AmfiService amfiService;
    private final BSEStarMasterDataService bseStarMasterDataService;
    private final MfFundSchemeService mfFundSchemeService;

    private final Pattern schemeCodePattern = Pattern.compile("\\d{6}");

    public Initializer(
            RestClient restClient,
            AmfiService amfiService,
            BSEStarMasterDataService bseStarMasterDataService,
            MfFundSchemeService mfFundSchemeService) {
        this.restClient = restClient;
        this.amfiService = amfiService;
        this.bseStarMasterDataService = bseStarMasterDataService;
        this.mfFundSchemeService = mfFundSchemeService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void handleApplicationStartedEvent() {
        LOGGER.info("Loading all Mutual Funds on StartUp");
        try {
            Map<String, Map<String, String>> amfiDataMap = amfiService.fetchAmfiSchemeData();
            long totalCount = mfFundSchemeService.getTotalCount();

            // Only proceed if there's more data to load
            if (amfiDataMap.size() > totalCount) {

                Map<String, String> amfiCodeIsinMapping = getAmfiCodeISINMapping(amfiDataMap);

                Map<String, MfFundScheme> bseStarMasterDataMap =
                        bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping);

                // Process data
                processMasterData(bseStarMasterDataMap, amfiDataMap);
                LOGGER.debug("Completed loading initial data.");
            }
        } catch (HttpClientErrorException | IOException httpClientErrorException) {
            LOGGER.error("Failed to load all Funds", httpClientErrorException);
        } catch (CsvException e) {
            LOGGER.error("Failed to process CSV data", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getAmfiCodeISINMapping(Map<String, Map<String, String>> amfiDataMap) {
        Map<String, String> amfiCodeIsinMapping = getAMFICodeISINMap();

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

                    // Map ISIN to AMFI code
                    amfiCodeIsinMapping.putIfAbsent(processedIsin, amfiCode);
                }
            }
        }
        return amfiCodeIsinMapping;
    }

    // Parallel processing for better performance
    private void processMasterData(
            Map<String, MfFundScheme> bseStarMasterDataMap, Map<String, Map<String, String>> amfiDataMap) {

        Set<String> distinctAmfiCodeFromDB = new HashSet<>(this.mfFundSchemeService.findDistinctAmfiCode());
        List<MfFundScheme> mfFundSchemeList = bseStarMasterDataMap.keySet().stream()
                .filter(amfiDataMap::containsKey)
                .filter(s -> !distinctAmfiCodeFromDB.contains(s))
                .distinct()
                .map(bseStarMasterDataMap::get)
                .toList();

        // Batch insert instead of inserting individually
        if (!mfFundSchemeList.isEmpty()) {
            mfFundSchemeService.saveData(mfFundSchemeList);
        }
    }

    private Map<String, String> getAMFICodeISINMap() {
        LOGGER.info("Downloading NAVAll from AMFI");
        String allNAVs = null;
        try {
            allNAVs = restClient
                    .get()
                    .uri(SchemeConstants.AMFI_WEBSITE_LINK)
                    .headers(HttpHeaders::clearContentHeaders)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            (request, response) ->
                                    LOGGER.error("Failed to retrieve AMFI codes {}", response.getStatusCode()))
                    .body(String.class);
        } catch (RestClientException e) {
            LOGGER.error("Failed to retrieve AMFI codes ", e);
        }
        return getAmfiCodeIsinMap(allNAVs);
    }

    private Map<String, String> getAmfiCodeIsinMap(String allNAVs) {
        Map<String, String> amfiCodeIsinMap = new HashMap<>();
        if (allNAVs != null && !allNAVs.isEmpty()) {
            for (String row : allNAVs.split("\n")) {
                Matcher matcher = schemeCodePattern.matcher(row);
                if (matcher.find()) {
                    String[] rowParts = row.split(";");
                    if (rowParts.length >= 3 && !rowParts[1].equals("-")) {
                        amfiCodeIsinMap.put(rowParts[1].trim(), rowParts[0].trim());
                    }
                    if (rowParts.length >= 4 && !rowParts[2].equals("-")) {
                        amfiCodeIsinMap.put(rowParts[2].trim(), rowParts[0].trim());
                    }
                }
            }
        }
        return amfiCodeIsinMap;
    }
}
