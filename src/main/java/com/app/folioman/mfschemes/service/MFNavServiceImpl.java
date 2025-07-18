package com.app.folioman.mfschemes.service;

import static com.app.folioman.mfschemes.util.SchemeConstants.FLEXIBLE_DATE_FORMATTER;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.LocalDateUtility;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Transactional(readOnly = true)
public class MFNavServiceImpl implements MFNavService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MFNavServiceImpl.class);

    private final CachedNavService cachedNavService;
    private final MfSchemeService mfSchemeService;
    private final MfHistoricalNavService historicalNavService;
    private final MFSchemeNavRepository mfSchemeNavRepository;
    private final MfFundSchemeRepository mFSchemeRepository;
    private final RestClient restClient;
    private final TransactionTemplate transactionTemplate;

    private final Pattern schemeCodePattern = Pattern.compile("\\d{6}");
    private final ApplicationProperties applicationProperties;

    MFNavServiceImpl(
            CachedNavService cachedNavService,
            MfSchemeService mfSchemeService,
            MfHistoricalNavService historicalNavService,
            MFSchemeNavRepository mfSchemeNavRepository,
            MfFundSchemeRepository mFSchemeRepository,
            RestClient restClient,
            PlatformTransactionManager transactionManager,
            ApplicationProperties applicationProperties) {
        this.cachedNavService = cachedNavService;
        this.mfSchemeService = mfSchemeService;
        this.historicalNavService = historicalNavService;
        this.mfSchemeNavRepository = mfSchemeNavRepository;
        this.mFSchemeRepository = mFSchemeRepository;
        this.restClient = restClient;
        // Create a new TransactionTemplate with the desired propagation behavior
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.applicationProperties = applicationProperties;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MFSchemeDTO getNav(Long schemeCode) {
        return getNavByDateWithRetry(schemeCode, LocalDateUtility.getAdjustedDate());
    }

    @Override
    public MFSchemeDTO getNavOnDate(Long schemeCode, LocalDate inputDate) {
        LocalDate adjustedDate = LocalDateUtility.getAdjustedDate(inputDate);
        return getNavByDateWithRetry(schemeCode, adjustedDate);
    }

    @Override
    public MFSchemeDTO getNavByDateWithRetry(Long schemeCode, LocalDate navDate) {
        LOGGER.info("Fetching Nav for AMFISchemeCode: {} for date: {} from Cache", schemeCode, navDate);
        MFSchemeDTO mfSchemeDTO;
        int retryCount = 0;

        while (true) {
            try {
                mfSchemeDTO = cachedNavService.getNavForDate(schemeCode, navDate);
                break; // Exit the loop if successful
            } catch (NavNotFoundException navNotFoundException) {
                LOGGER.error("NavNotFoundException occurred: {}", navNotFoundException.getMessage());

                LocalDate currentNavDate = navNotFoundException.getDate();
                if (retryCount == SchemeConstants.FIRST_RETRY || retryCount == SchemeConstants.THIRD_RETRY) {
                    // make a call to get historical Data and persist
                    String oldSchemeCode = historicalNavService.getHistoricalNav(schemeCode, navDate);
                    if (StringUtils.hasText(oldSchemeCode)) {
                        mfSchemeService.fetchSchemeDetails(oldSchemeCode, schemeCode);
                        currentNavDate = LocalDateUtility.getAdjustedDate(currentNavDate.plusDays(2));
                    } else {
                        // NFO scenario where data is not present in historical data, hence load all available data
                        mfSchemeService.fetchSchemeDetails(String.valueOf(schemeCode), schemeCode);
                    }
                }
                // retrying 4 times
                if (retryCount >= SchemeConstants.MAX_RETRIES) {
                    throw navNotFoundException;
                }

                retryCount++;
                navDate = LocalDateUtility.getAdjustedDate(currentNavDate.minusDays(1));
                LOGGER.info("Retrying for date: {} for scheme: {}", navDate, schemeCode);
            }
        }
        return mfSchemeDTO;
    }

    @Override
    public void loadLastDayDataNav() {
        List<Long> historicalDataNotLoadedSchemeIdList = getHistoricalDataNotLoadedSchemeIdList();
        if (!historicalDataNotLoadedSchemeIdList.isEmpty()) {
            String allNAVs = downloadAllNAVs();
            Map<Long, NavHolder> amfiCodeNavMap = getAmfiCodeNavMap(allNAVs);
            List<MFSchemeNav> mfSchemeNavList = historicalDataNotLoadedSchemeIdList.stream()
                    .filter(amfiCodeNavMap::containsKey)
                    .map(amfiCode -> {
                        NavHolder navHolder = amfiCodeNavMap.get(amfiCode);
                        MFSchemeNav mfSchemeNav = new MFSchemeNav();
                        mfSchemeNav.setNav(navHolder.nav);
                        mfSchemeNav.setNavDate(navHolder.navDate);
                        mfSchemeNav.setMfScheme(mFSchemeRepository.getReferenceByAmfiCode(amfiCode));
                        return mfSchemeNav;
                    })
                    .toList();

            if (!mfSchemeNavList.isEmpty()) {
                transactionTemplate.execute(status -> mfSchemeNavRepository.saveAll(mfSchemeNavList));
            }
        }
    }

    @Override
    public void loadHistoricalDataIfNotExists() {
        List<Long> historicalDataNotLoadedSchemeIdList = getHistoricalDataNotLoadedSchemeIdList();
        if (!historicalDataNotLoadedSchemeIdList.isEmpty()) {
            for (Long schemeId : historicalDataNotLoadedSchemeIdList) {
                mfSchemeService.fetchSchemeDetails(schemeId);
            }
            LOGGER.info("Completed loading HistoricalData for schemes that don't exist");
        }
    }

    @Override
    public Map<String, String> getAmfiCodeIsinMap() {
        String downloadedAllNAVs = downloadAllNAVs();
        return getAmfiCodeIsinMap(downloadedAllNAVs);
    }

    /**
     * Process NAVs for a list of scheme codes asynchronously.
     * This method should handle parallel processing and transactional boundaries.
     */
    @Override
    @Async("taskExecutor")
    public void processNavsAsync(List<Long> schemeCodes) {
        if (schemeCodes == null || schemeCodes.isEmpty()) {
            LOGGER.info("No scheme codes provided for NAV processing.");
            return;
        }
        LOGGER.info("Processing NAVs asynchronously for scheme codes: {}", schemeCodes);
        for (Long schemeCode : schemeCodes) {
            try {
                LOGGER.info("Processing NAV for scheme code: {}", schemeCode);
                getNav(schemeCode);
            } catch (Exception e) {
                LOGGER.error("Error processing NAV for scheme code: {}", schemeCode, e);
            }
        }
    }

    @Override
    public Map<Long, Map<LocalDate, MFSchemeNavProjection>> getNavsForSchemesAndDates(
            Set<Long> schemeCodes, LocalDate startDate, LocalDate endDate) {
        // Try to prefetch NAVs but handle exceptions for each scheme code individually
        for (Long schemeCode : schemeCodes) {
            try {
                getNav(schemeCode);
            } catch (NavNotFoundException e) {
                // Log the exception but continue with other scheme codes
                LOGGER.warn(
                        "Could not find NAV for scheme {}: {}. Continuing with other schemes.",
                        schemeCode,
                        e.getMessage());
            } catch (Exception e) {
                // Log any other exceptions but continue with other scheme codes
                LOGGER.error(
                        "Error while fetching NAV for scheme {}: {}. Continuing with other schemes.",
                        schemeCode,
                        e.getMessage());
            }
        }

        // Fetch NAVs in bulk for all schemes and dates
        LOGGER.info("Fetching Nav for amfiCodes: {} from {} to {}", schemeCodes, startDate, endDate);
        return mfSchemeNavRepository
                .findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                        schemeCodes, startDate, endDate)
                .stream()
                .collect(Collectors.groupingBy(
                        MFSchemeNavProjection::amfiCode,
                        Collectors.toMap(MFSchemeNavProjection::navDate, Function.identity())));
    }

    private Map<String, String> getAmfiCodeIsinMap(String allNAVs) {
        Map<String, String> amfiCodeIsinMap = new HashMap<>();
        if (allNAVs != null && !allNAVs.isEmpty()) {
            for (String row : allNAVs.split("\n")) {
                Matcher matcher = schemeCodePattern.matcher(row);
                if (matcher.find()) {
                    String[] rowParts = row.split(SchemeConstants.NAV_SEPARATOR);
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

    private String downloadAllNAVs() {
        LOGGER.info("Downloading NAVAll from AMFI");
        String allNAVs = null;
        try {
            allNAVs = restClient
                    .get()
                    .uri(applicationProperties.getNav().getAmfi().getDataUrl())
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
        return allNAVs;
    }

    private Map<Long, NavHolder> getAmfiCodeNavMap(String allNAVs) {
        Map<Long, NavHolder> amfiCodeIsinMap = new HashMap<>();
        if (allNAVs != null && !allNAVs.isEmpty()) {
            for (String row : allNAVs.split("\n")) {
                Matcher matcher = schemeCodePattern.matcher(row);
                if (matcher.find()) {
                    String[] rowParts = row.split(SchemeConstants.NAV_SEPARATOR);
                    String nav = rowParts[4].strip();
                    LocalDate navDate = LocalDate.parse(rowParts[5].strip(), FLEXIBLE_DATE_FORMATTER);
                    if (navDate.isEqual(LocalDateUtility.getYesterday())) {
                        amfiCodeIsinMap.put(
                                Long.valueOf(rowParts[0].strip()),
                                new NavHolder("N.A.".equals(nav) ? BigDecimal.ZERO : new BigDecimal(nav), navDate));
                    }
                }
            }
        }
        return amfiCodeIsinMap;
    }

    /**
     * Retrieves a list of scheme IDs for which historical data has not been loaded.
     *
     * @return A List of Long values representing the scheme IDs without loaded historical data.
     */
    private List<Long> getHistoricalDataNotLoadedSchemeIdList() {
        LocalDate yesterday = LocalDateUtility.getYesterday();
        return mfSchemeNavRepository.findMFSchemeNavsByNavNotLoaded(yesterday);
    }

    private record NavHolder(BigDecimal nav, LocalDate navDate) {}
}
