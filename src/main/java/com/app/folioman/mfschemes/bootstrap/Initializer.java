package com.app.folioman.mfschemes.bootstrap;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.service.AmfiService;
import com.app.folioman.mfschemes.service.BSEStarMasterDataService;
import com.app.folioman.mfschemes.service.MfFundSchemeService;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

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
                Map<String, String> amfiCodeIsinMapping = getAMFICodeISINMap();
                Map<String, MfFundScheme> bseStarMasterDataMap =
                        bseStarMasterDataService.fetchBseStarMasterData(amfiDataMap, amfiCodeIsinMapping);

                // Process data
                processMasterData(bseStarMasterDataMap, amfiCodeIsinMapping);
                LOGGER.debug("Completed loading initial data.");
            }
        } catch (HttpClientErrorException | ResourceAccessException | IOException httpClientErrorException) {
            LOGGER.error("Failed to load all Funds", httpClientErrorException);
        } catch (CsvException e) {
            LOGGER.error("Failed to process CSV data", e);
            throw new RuntimeException(e);
        }
    }

    // Parallel processing for better performance
    private void processMasterData(
            Map<String, MfFundScheme> bseStarMasterDataMap, Map<String, String> amfiCodeIsinMapping) {

        List<String> distinctAmfiCodeFromDB = this.mfFundSchemeService.findDistinctAmfiCode();
        List<MfFundScheme> mfFundSchemeList = bseStarMasterDataMap.keySet().parallelStream() // use parallel stream
                .filter(amfiCodeIsinMapping::containsValue)
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
        String allNAVs = restClient
                .get()
                .uri(SchemeConstants.AMFI_WEBSITE_LINK)
                .headers(HttpHeaders::clearContentHeaders)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .retrieve()
                .body(String.class);
        return getAmfiCodeIsinMap(allNAVs);
    }

    private Map<String, String> getAmfiCodeIsinMap(String allNAVs) {
        Map<String, String> amfiCodeIsinMap = new HashMap<>();
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
        return amfiCodeIsinMap;
    }
}
