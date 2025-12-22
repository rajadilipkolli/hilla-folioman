package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.SchemeNotFoundException;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.app.folioman.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.app.folioman.mfschemes.models.projection.NavDateValueProjection;
import com.app.folioman.mfschemes.models.response.NavResponse;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Transactional(readOnly = true)
@Service
class MfSchemeServiceImpl implements MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeServiceImpl.class);

    // Improved concurrent processing: Use a concurrent map of scheme IDs to monitor
    // objects for synchronization
    private static final Map<Long, Object> SCHEME_LOCKS = new ConcurrentHashMap<>();

    private final RestClient restClient;
    private final MfFundSchemeRepository mFSchemeRepository;
    private final MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper;
    private final SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationProperties applicationProperties;
    private final MFSchemeNavRepository mfSchemeNavRepository;
    private final MfAmcService mfAmcService;

    MfSchemeServiceImpl(
            RestClient restClient,
            MfFundSchemeRepository mFSchemeRepository,
            MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper,
            SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper,
            PlatformTransactionManager transactionManager,
            ApplicationProperties applicationProperties,
            MFSchemeNavRepository mfSchemeNavRepository,
            MfAmcService mfAmcService) {
        this.restClient = restClient;
        this.mFSchemeRepository = mFSchemeRepository;
        this.mfSchemeEntityToDtoMapper = mfSchemeEntityToDtoMapper;
        this.schemeNAVDataDtoToEntityMapper = schemeNAVDataDtoToEntityMapper;
        // Create a new TransactionTemplate with the desired propagation behavior
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.mfSchemeNavRepository = mfSchemeNavRepository;
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.applicationProperties = applicationProperties;
        this.mfAmcService = mfAmcService;
    }

    public long count() {
        return mFSchemeRepository.count();
    }

    public List<Long> findAllSchemeIds() {
        return mFSchemeRepository.findAllSchemeIds();
    }

    @Transactional
    public List<MfFundScheme> saveAllEntities(List<MfFundScheme> list) {
        return mFSchemeRepository.saveAll(list);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MfFundScheme saveEntity(MfFundScheme mfScheme) {
        return mFSchemeRepository.save(mfScheme);
    }

    public Optional<MfFundScheme> findBySchemeCode(Long schemeCode) {
        return Optional.ofNullable(this.mFSchemeRepository.findByAmfiCode(schemeCode));
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        Optional<MfFundScheme> bySchemeIdAndMfSchemeNavsNavDate =
                this.mFSchemeRepository.findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, navDate);

        return bySchemeIdAndMfSchemeNavsNavDate.map(mfSchemeEntityToDtoMapper::convertEntityToDto);
    }

    @Override
    public Optional<MFSchemeProjection> findByPayOut(String isin) {
        return mFSchemeRepository.findByIsin(isin);
    }

    @Override
    public List<FundDetailProjection> fetchSchemes(String query) {
        query = query.strip();

        // If query is numeric and 6 digits, it might be an AMFI code
        if (query.matches("\\d{6}")) {
            LOGGER.info("Fetching scheme by AMFI code: {}", query);
            Long amfiCode = Long.parseLong(query);
            MfFundScheme scheme = this.mFSchemeRepository.findByAmfiCode(amfiCode);
            if (scheme != null) {
                return List.of(createFundDetailProjection(scheme));
            }
            return List.of();
        }

        String[] keywords = query.split("\\s+");

        // Default full-text search for scheme name
        String sName;
        if (keywords.length < 2) {
            sName = query;
        } else {
            // Create a proper full-text search query with all terms required
            sName = formatTsQueryTerms(query);
            LOGGER.info("Using formatted search terms: {}", sName);
        }

        // Get full-text search results
        List<FundDetailProjection> results = this.mFSchemeRepository.searchByFullText(sName);
        LOGGER.info("Returning {} search results for query: {}", results.size(), query);
        if (!results.isEmpty()) {
            return results;
        }

        // If no results, try searching by AMC name
        String queryLower = query.toLowerCase();

        // Check if query might specifically be searching for an AMC
        boolean containsAmcKeyword =
                queryLower.contains("amc") || queryLower.contains("asset") || queryLower.contains("management");

        // Try AMC search if it contains AMC keywords
        if (containsAmcKeyword) {
            List<FundDetailProjection> amcResults = searchByAmc(query, queryLower);
            if (!amcResults.isEmpty()) {
                return amcResults;
            }
        }

        // Return empty list when no results found
        return List.of();
    }

    private FundDetailProjection createFundDetailProjection(MfFundScheme scheme) {
        return new FundDetailProjection() {
            @Override
            public String getSchemeName() {
                return scheme.getName();
            }

            @Override
            public Long getAmfiCode() {
                return scheme.getAmfiCode();
            }

            @Override
            public String getAmcName() {
                return scheme.getAmc() != null ? scheme.getAmc().getName() : "Unknown";
            }
        };
    }

    private List<FundDetailProjection> searchByAmc(String query, String queryLower) {
        LOGGER.info("Fetching schemes by AMC name: {}", query);

        // First try direct AMC search with the original query
        List<FundDetailProjection> amcResults = this.mFSchemeRepository.searchByAmc(query);
        if (!amcResults.isEmpty()) {
            return amcResults;
        }

        // Extract search terms for AMC search, removing AMC-specific keywords
        String amcSearchTerms = queryLower
                .replaceAll("\\s*(amc|asset|management)\\s*", " ")
                .replaceAll("\\s+", " ")
                .strip();

        if (StringUtils.hasText(amcSearchTerms)) {
            // Try with AMC text search using the formatted query
            String tsQuery = formatTsQueryTerms(amcSearchTerms);
            if (StringUtils.hasText(tsQuery)) {
                LOGGER.info("Trying AMC text search with: {}", tsQuery);
                amcResults = this.mFSchemeRepository.searchByAmcTextSearch(tsQuery);
                if (!amcResults.isEmpty()) {
                    return amcResults;
                }
            }

            // If no match yet, try fuzzy search through the AMC service
            List<MfAmc> matchingAmcs = mfAmcService.findBySearchTerms(amcSearchTerms);
            if (!matchingAmcs.isEmpty()) {
                LOGGER.info(
                        "Found AMC match using fuzzy search: {}",
                        matchingAmcs.getFirst().getName());

                // Use the AMC name directly for searching schemes
                amcResults = this.mFSchemeRepository.searchByAmc(
                        matchingAmcs.getFirst().getName());
                if (!amcResults.isEmpty()) {
                    return amcResults;
                }
            }
        }

        return List.of();
    }

    /**
     * Format search terms for PostgreSQL ts_query
     *
     * @param terms Space-separated search terms
     * @return Formatted terms in format: term1 & term2 & term3
     */
    private String formatTsQueryTerms(String terms) {
        if (terms == null || terms.isEmpty()) {
            return "";
        }

        return Arrays.stream(terms.split("\\s+"))
                .map(term -> term.replaceAll("[^a-zA-Z0-9]", "")) // Remove special characters
                .filter(term -> !term.isEmpty())
                .collect(Collectors.joining(" & "));
    }

    @Override
    public void fetchSchemeDetails(Long schemeId) {
        NavResponse navResponse = getNavResponseResponseEntity(schemeId);
        processResponseEntity(schemeId, navResponse);
    }

    @Override
    public List<MFSchemeProjection> fetchSchemesByRtaCode(String rtaCode) {
        return this.mFSchemeRepository.findByRtaCodeStartsWith(rtaCode);
    }

    @Override
    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        NavResponse navResponse = getNavResponseResponseEntity(Long.valueOf(oldSchemeCode));
        processResponseEntity(newSchemeCode, navResponse);
    }

    private void processResponseEntity(Long schemeCode, NavResponse navResponse) {
        boolean existsByAmfiCode = this.mFSchemeRepository.existsByAmfiCode(schemeCode);
        if (existsByAmfiCode) {
            mergeList(navResponse, this.mFSchemeRepository.findByAmfiCode(schemeCode), schemeCode);
        } else {
            // Scenario where scheme is discontinued or merged with other
            LOGGER.error("Found Discontinued SchemeCode : {}", schemeCode);
        }
    }

    /**
     * Execute an operation with scheme-specific synchronization to ensure thread
     * safety
     * while minimizing contention between different schemes.
     *
     * @param schemeCode The scheme code to synchronize on
     * @param operation  The operation to execute
     * @param <T>        The return type of the operation
     * @return The result of the operation
     */
    private <T> T withSchemeLock(Long schemeCode, Supplier<T> operation) {
        // Get or create a lock object for this specific scheme
        Object lock = SCHEME_LOCKS.computeIfAbsent(schemeCode, k -> new Object());

        synchronized (lock) {
            try {
                return operation.get();
            } finally {
                // Remove the lock if no more operations are pending for this scheme
                // This prevents memory leaks from accumulating lock objects
                if (SCHEME_LOCKS.size() > 100) { // Only clean up when the map gets large
                    SCHEME_LOCKS.remove(schemeCode);
                }
            }
        }
    }

    private void mergeList(NavResponse navResponse, MfFundScheme mfFundScheme, Long schemeCode) {
        // Skip processing if there's no new data to merge
        if (navResponse.data().isEmpty()) {
            LOGGER.info("No NAV data received for scheme {}", schemeCode);
            return;
        }

        // Execute the merge operation with scheme-specific synchronization
        withSchemeLock(schemeCode, () -> {
            // Check if NAV data is already up to date to avoid unnecessary processing
            if (navResponse.data().size() == mfFundScheme.getMfSchemeNavs().size()) {
                LOGGER.info("Data in DB and from api is same, no updates needed for scheme {}", schemeCode);
                return null;
            }

            // Data from 3rd Party API
            List<MFSchemeNav> newNavEntries = navResponse.data().stream()
                    .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                    .map(schemeNAVDataDtoToEntityMapper::schemeNAVDataDTOToEntity)
                    .toList();
            LOGGER.info("No of entries from API Server: {} for schemeCode/amfi: {}", newNavEntries.size(), schemeCode);

            // Fetch all existing NAV dates for this scheme in a single query
            // This avoids loading full entity objects when we only need dates for
            // comparison
            List<NavDateValueProjection> existingNavs =
                    mfSchemeNavRepository.findAllNavDateValuesBySchemeId(mfFundScheme.getId());

            // Filter out NAVs that already exist in the database
            List<MFSchemeNav> navsToSave = newNavEntries.stream()
                    .filter(newNav ->
                            !existingNavs.contains(new NavDateValueProjection(newNav.getNav(), newNav.getNavDate())))
                    .peek(newNav -> newNav.setMfScheme(mfFundScheme))
                    .toList();

            if (navsToSave.isEmpty()) {
                LOGGER.info("All NAVs already exist in database for scheme {}", schemeCode);
                return null;
            }

            // Use transaction to ensure database consistency
            transactionTemplate.execute(status -> {
                try {
                    LOGGER.info("Saving {} new NAVs for scheme {}", navsToSave.size(), schemeCode);

                    // Try batch save first - most efficient approach
                    try {
                        mfSchemeNavRepository.saveAll(navsToSave);
                    } catch (DataIntegrityViolationException ex) {
                        // When batch insert fails, use a more efficient partitioning approach
                        LOGGER.warn("Batch insert failed, switching to partitioned approach: {}", ex.getMessage());
                        saveNavsInBatches(navsToSave, 50); // Save in smaller batches of 50
                    }

                    return null;
                } catch (Exception e) {
                    LOGGER.error("Error while saving NAVs for scheme {}: {}", schemeCode, e.getMessage(), e);
                    throw e; // Rethrow to trigger transaction rollback
                }
            });

            return null;
        });
    }

    /**
     * Save NAVs in batches of specified size to handle potential constraint
     * violations more efficiently.
     *
     * @param navs      The list of NAVs to save
     * @param batchSize The size of each batch
     */
    private void saveNavsInBatches(List<MFSchemeNav> navs, int batchSize) {
        // Process in smaller batches instead of recursive partitioning
        for (int i = 0; i < navs.size(); i += batchSize) {
            List<MFSchemeNav> batch = navs.subList(i, Math.min(i + batchSize, navs.size()));
            try {
                mfSchemeNavRepository.saveAll(batch);
                LOGGER.debug("Successfully saved batch of {} NAVs", batch.size());
            } catch (DataIntegrityViolationException ex) {
                // If a smaller batch still fails, save items individually
                LOGGER.warn("Batch of size {} failed, falling back to individual saves", batch.size());
                batch.forEach(nav -> {
                    try {
                        mfSchemeNavRepository.save(nav);
                    } catch (DataIntegrityViolationException e) {
                        LOGGER.debug("Skipped duplicate NAV entry for date: {}", nav.getNavDate());
                    }
                });
            }
        }
    }

    private NavResponse getNavResponseResponseEntity(Long schemeCode) {
        return this.restClient
                .get()
                .uri(getUri(schemeCode))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    LOGGER.error(
                            "Error fetching NAV response for schemeCode: {} with stack : {}", schemeCode, response);
                    throw new SchemeNotFoundException("scheme with id %d not found".formatted(schemeCode));
                })
                .body(NavResponse.class);
    }

    private URI getUri(Long schemeCode) {
        LOGGER.info("Fetching SchemeDetails for AMFISchemeCode: {}", schemeCode);
        return UriComponentsBuilder.fromUriString(
                        applicationProperties.getNav().getMfApi().getDataUrl())
                .buildAndExpand(schemeCode)
                .toUri();
    }
}
