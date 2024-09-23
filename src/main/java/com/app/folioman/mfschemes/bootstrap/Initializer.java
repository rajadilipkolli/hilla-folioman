package com.app.folioman.mfschemes.bootstrap;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeDtoToEntityMapperHelper;
import com.app.folioman.mfschemes.service.AmfiService;
import com.app.folioman.mfschemes.service.BSEStarMasterDataService;
import com.app.folioman.mfschemes.service.MfFundSchemeService;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.LocalDateUtility;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.time.LocalDate;
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
    private final MfSchemeDtoToEntityMapperHelper mfSchemeDtoToEntityMapperHelper;

    private final Pattern schemeCodePattern = Pattern.compile("\\d{6}");

    public Initializer(
            RestClient restClient,
            AmfiService amfiService,
            BSEStarMasterDataService bseStarMasterDataService,
            MfFundSchemeService mfFundSchemeService,
            MfSchemeDtoToEntityMapperHelper mfSchemeDtoToEntityMapperHelper) {
        this.restClient = restClient;
        this.amfiService = amfiService;
        this.bseStarMasterDataService = bseStarMasterDataService;
        this.mfFundSchemeService = mfFundSchemeService;
        this.mfSchemeDtoToEntityMapperHelper = mfSchemeDtoToEntityMapperHelper;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void handleApplicationStartedEvent() {
        LOGGER.info("Loading all Mutual Funds on StartUp");
        try {
            Map<String, MfFundScheme> bseStarMasterDataMap = bseStarMasterDataService.fetchBseStarMasterData();
            Map<String, Map<String, String>> amfiDataMap = amfiService.fetchAmfiSchemeData();
            Map<String, String> amfiCodeIsinMapping = getAMFICodeISINMap();

            LocalDate endDateCutoff = LocalDate.now().minusWeeks(1);

            long totalCount = mfFundSchemeService.getTotalCount();
            if (amfiDataMap.size() > totalCount) {
                processMasterData(bseStarMasterDataMap, amfiCodeIsinMapping, amfiDataMap, endDateCutoff);
            }
        } catch (HttpClientErrorException | ResourceAccessException | IOException httpClientErrorException) {
            LOGGER.error("Failed to load all Funds", httpClientErrorException);
        } catch (CsvException e) {
            LOGGER.error("Failed to process CSV data", e);
            throw new RuntimeException(e);
        }
    }

    private void processMasterData(
            Map<String, MfFundScheme> bseStarMasterDataMap,
            Map<String, String> amfiCodeIsinMapping,
            Map<String, Map<String, String>> amfiDataMap,
            LocalDate endDateCutoff) {
        List<String> distinctIsinFromDB = this.mfFundSchemeService.findDistinctIsin();
        List<MfFundScheme> mfFundSchemeList = bseStarMasterDataMap.keySet().stream()
                .filter(amfiCodeIsinMapping::containsValue)
                .filter(s -> !distinctIsinFromDB.contains(s))
                .map(isinFromBSE -> {
                    MfFundScheme mfFundScheme = bseStarMasterDataMap.get(isinFromBSE);
                    String amfiCode = amfiCodeIsinMapping.get(isinFromBSE);
                    LocalDate schemeEndDate = null;

                    if (amfiCode != null) {
                        Map<String, String> amfiSchemeData = amfiDataMap.get(amfiCode);
                        if (amfiSchemeData != null) {
                            setMfSchemeCategory(amfiSchemeData, mfFundScheme);
                            String endDate = amfiSchemeData.get("Closure Date");
                            if (endDate != null && !endDate.isBlank()) {
                                LocalDate closureDate = LocalDateUtility.parse(endDate);
                                if (closureDate.isBefore(endDateCutoff)) {
                                    schemeEndDate = closureDate;
                                }
                            }
                        }
                        mfFundScheme.setAmfiCode(Long.valueOf(amfiCode));
                    } else {
                        // amfiCode is NUll.
                        LOGGER.warn("No AMFI code found for ISIN: {}", isinFromBSE);
                    }

                    // Save or update the fund scheme in the database
                    mfFundScheme.setEndDate(schemeEndDate);
                    return mfFundScheme;
                })
                .toList();
        if (!mfFundSchemeList.isEmpty()) {
            mfFundSchemeService.saveData(mfFundSchemeList);
        }
    }

    private void setMfSchemeCategory(Map<String, String> amfiSchemeData, MfFundScheme mfFundScheme) {
        String catStr = amfiSchemeData.get("Scheme Category");
        String catSchemeType = amfiSchemeData.get("Scheme Type");
        String category;
        String subcategory;
        if (catStr.contains("-")) {
            category = catStr.substring(0, catStr.indexOf("-")).strip();
            subcategory = catStr.substring(catStr.indexOf("-") + 1).strip();
        } else {
            category = catStr;
            subcategory = null;
        }
        MFSchemeType mfSchemeTypeEntity =
                mfSchemeDtoToEntityMapperHelper.findOrCreateMFSchemeTypeEntity(catSchemeType, category, subcategory);
        mfFundScheme.getAmc().setName(amfiSchemeData.get("AMC"));
        mfFundScheme.setMfSchemeType(mfSchemeTypeEntity);
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
