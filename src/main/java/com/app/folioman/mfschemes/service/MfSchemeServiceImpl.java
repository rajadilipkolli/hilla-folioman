package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.SchemeNotFoundException;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.app.folioman.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.app.folioman.mfschemes.models.response.NavResponse;
import com.app.folioman.mfschemes.models.response.SchemeNAVDataDTO;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Transactional(readOnly = true)
@Service
public class MfSchemeServiceImpl implements MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeServiceImpl.class);

    private final RestClient restClient;
    private final MfFundSchemeRepository mFSchemeRepository;
    private final MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper;
    private final SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationProperties applicationProperties;
    private final MFSchemeNavRepository mfSchemeNavRepository;

    public MfSchemeServiceImpl(
            RestClient restClient,
            MfFundSchemeRepository mFSchemeRepository,
            MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper,
            SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper,
            PlatformTransactionManager transactionManager,
            ApplicationProperties applicationProperties,
            MFSchemeNavRepository mfSchemeNavRepository) {
        this.restClient = restClient;
        this.mFSchemeRepository = mFSchemeRepository;
        this.mfSchemeEntityToDtoMapper = mfSchemeEntityToDtoMapper;
        this.schemeNAVDataDtoToEntityMapper = schemeNAVDataDtoToEntityMapper;
        // Create a new TransactionTemplate with the desired propagation behavior
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.mfSchemeNavRepository = mfSchemeNavRepository;
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.applicationProperties = applicationProperties;
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
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String[] keywords = schemeName.strip().split("\\s");

        // Join the keywords with " & " and wrap each with single quotes
        String sName;
        if (keywords.length < 2) {
            sName = schemeName;
        } else {
            sName = Arrays.stream(keywords).map(keyword -> "'" + keyword + "'").collect(Collectors.joining(" & "));
        }
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mFSchemeRepository.searchByFullText(sName);
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

    private void mergeList(NavResponse navResponse, MfFundScheme mfFundScheme, Long schemeCode) {
        // Early exit if no changes needed
        if (navResponse.data().size() == mfFundScheme.getMfSchemeNavs().size()) {
            LOGGER.info("Data in DB and from service is same, no update needed for scheme: {}", schemeCode);
            return;
        }

        // Calculate difference efficiently
        LOGGER.info(
                "Processing {} NAV entries from api server for schemeCode: {}",
                navResponse.data().size(),
                schemeCode);

        // Create a set of existing NAV dates to efficiently check for duplicates
        final Set<LocalDate> existingNavDates = mfFundScheme.getMfSchemeNavs().stream()
                .map(MFSchemeNav::getNavDate)
                .collect(Collectors.toSet());

        // Process in batches to reduce memory usage
        final int BATCH_SIZE = 100; // Configured batch size
        final List<List<MFSchemeNav>> batches = new ArrayList<>();
        List<MFSchemeNav> currentBatch = new ArrayList<>(BATCH_SIZE);

        int newNavsCount = 0;

        // Filter and map in a single pass, collecting into batches
        for (SchemeNAVDataDTO navDataDTO : navResponse.data()) {
            // Skip if this NAV date already exists
            if (existingNavDates.contains(navDataDTO.date())) {
                continue;
            }

            // Create new NAV entity
            MFSchemeNav newNav =
                    schemeNAVDataDtoToEntityMapper.schemeNAVDataDTOToEntity(navDataDTO.withSchemeId(schemeCode));

            currentBatch.add(newNav);
            newNavsCount++;

            // When batch is full, add to batches and create a new batch
            if (currentBatch.size() >= BATCH_SIZE) {
                batches.add(currentBatch);
                currentBatch = new ArrayList<>(BATCH_SIZE);
            }
        }

        // Add final batch if not empty
        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        LOGGER.info("Found {} new NAV entries to insert for schemeCode: {}", newNavsCount, schemeCode);

        if (newNavsCount > 0) {
            // Process each batch in a separate transaction
            AtomicInteger successCount = new AtomicInteger(0);

            for (List<MFSchemeNav> batch : batches) {
                try {
                    // Process each batch in a new transaction
                    int batchSuccessCount = transactionTemplate.execute(status -> {
                        int count = 0;
                        List<MFSchemeNav> mfSchemeNavList = batch.stream()
                                .map(mfSchemeNav -> mfSchemeNav.setMfScheme(mfFundScheme))
                                .toList();
                        try {
                            // Save all NAV entries in the batch
                            List<MFSchemeNav> mfSchemeNavs = mfSchemeNavRepository.saveAll(mfSchemeNavList);
                            count = mfSchemeNavs.size();
                        } catch (DataIntegrityViolationException e) {
                            // When batch insert fails, use a partitioning approach instead of individual saves
                            LOGGER.warn("Batch insert failed due to constraint violations: {}", e.getMessage());
                            // Use a binary partitioning approach to save NAVs in smaller batches
                            processNavsInPartitions(mfSchemeNavList, mfFundScheme.getId());
                        }
                        return count;
                    });

                    successCount.addAndGet(batchSuccessCount);
                } catch (Exception e) {
                    LOGGER.error("Error processing NAV batch for scheme: {}", schemeCode, e);
                }
            }

            LOGGER.info("Successfully inserted {} new NAV entries for schemeCode: {}", successCount.get(), schemeCode);
        }
    }

    /**
     * Process NAVs in partitions to handle large batches more efficiently when constraint violations occur.
     * Uses a divide-and-conquer approach to minimize database calls while handling duplicates.
     *
     * @param navs The list of NAVs to save
     * @param schemeId The scheme ID these NAVs belong to
     */
    private void processNavsInPartitions(List<MFSchemeNav> navs, Long schemeId) {
        Stack<List<MFSchemeNav>> stack = new Stack<>();
        stack.push(navs);

        while (!stack.isEmpty()) {
            List<MFSchemeNav> currentNavs = stack.pop();

            if (currentNavs.size() <= 5) {
                currentNavs.forEach(nav -> {
                    try {
                        mfSchemeNavRepository.save(nav);
                    } catch (DataIntegrityViolationException e) {
                        LOGGER.warn(
                                "Skipping duplicate NAV entry: {} for date: {} and scheme: {}",
                                nav.getNav(),
                                nav.getNavDate(),
                                schemeId);
                    }
                });
                continue;
            }

            int mid = currentNavs.size() / 2;
            List<MFSchemeNav> firstHalf = currentNavs.subList(0, mid);
            List<MFSchemeNav> secondHalf = currentNavs.subList(mid, currentNavs.size());

            try {
                mfSchemeNavRepository.saveAll(firstHalf);
            } catch (DataIntegrityViolationException ex) {
                stack.push(firstHalf);
            }

            try {
                mfSchemeNavRepository.saveAll(secondHalf);
            } catch (DataIntegrityViolationException ex) {
                stack.push(secondHalf);
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
