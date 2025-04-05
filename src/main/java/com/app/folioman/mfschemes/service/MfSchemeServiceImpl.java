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
import com.app.folioman.mfschemes.models.projection.NavDateValueProjection;
import com.app.folioman.mfschemes.models.response.NavResponse;
import com.app.folioman.mfschemes.repository.MFSchemeNavRepository;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        if (navResponse.data().size() != mfFundScheme.getMfSchemeNavs().size()) {
            // Data from 3rd Party API
            List<MFSchemeNav> navList = navResponse.data().stream()
                    .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                    .map(schemeNAVDataDtoToEntityMapper::schemeNAVDataDTOToEntity)
                    .toList();
            LOGGER.info("No of entries from API Server :{} for schemeCode/amfi :{}", navList.size(), schemeCode);

            // Find only the new NAVs that don't already exist in the database
            List<MFSchemeNav> newNavs = navList.stream()
                    .filter(nav -> !mfFundScheme.getMfSchemeNavs().contains(nav))
                    .toList();

            LOGGER.info("No of entities to insert :{} for schemeCode/amfi :{}", newNavs.size(), schemeCode);

            if (!newNavs.isEmpty()) {
                try {
                    transactionTemplate.execute(status -> {
                        // Fetch all existing NAVs for this scheme in a single query to avoid N+1 problem
                        List<NavDateValueProjection> existingNavs =
                                mfSchemeNavRepository.findAllNavDateValuesBySchemeId(mfFundScheme.getId());

                        // Create a batch of NAVs that don't exist yet
                        List<MFSchemeNav> navsToSave = newNavs.stream()
                                .filter(newNav -> {
                                    // Create a projection for comparison
                                    NavDateValueProjection projection =
                                            new NavDateValueProjection(newNav.getNav(), newNav.getNavDate());
                                    return !existingNavs.contains(projection);
                                })
                                .peek(newNav -> newNav.setMfScheme(mfFundScheme))
                                .toList();

                        if (!navsToSave.isEmpty()) {
                            LOGGER.info("Saving {} NAVs in batch for scheme {}", navsToSave.size(), schemeCode);
                            try {
                                // Save all NAVs in a single batch operation
                                mfSchemeNavRepository.saveAll(navsToSave);
                            } catch (DataIntegrityViolationException ex) {
                                // When batch insert fails, use a partitioning approach instead of individual saves
                                LOGGER.warn("Batch insert failed due to constraint violations: {}", ex.getMessage());

                                // Use a binary partitioning approach to save NAVs in smaller batches
                                processNavsInPartitions(navsToSave, mfFundScheme.getId());
                            }
                        } else {
                            LOGGER.info(
                                    "All {} NAVs already exist in database for scheme {}", newNavs.size(), schemeCode);
                        }

                        return null;
                    });

                    // Refresh the fund scheme to get updated NAVs
                    mFSchemeRepository.findById(mfFundScheme.getId());

                } catch (Exception e) {
                    LOGGER.error("Error while saving NAVs for scheme {}: {}", schemeCode, e.getMessage(), e);
                }
            }
        } else {
            LOGGER.info("Data in DB and from service is same, no updates needed");
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
        // If the list is small enough, try to save each item individually
        if (navs.size() <= 5) {
            navs.forEach(nav -> {
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
            return;
        }

        // Split the list in half and process each half
        int mid = navs.size() / 2;
        List<MFSchemeNav> firstHalf = navs.subList(0, mid);
        List<MFSchemeNav> secondHalf = navs.subList(mid, navs.size());

        // Try to save each half as a batch
        try {
            mfSchemeNavRepository.saveAll(firstHalf);
        } catch (DataIntegrityViolationException ex) {
            // If saving the first half fails, process it recursively in smaller batches
            processNavsInPartitions(firstHalf, schemeId);
        }

        try {
            mfSchemeNavRepository.saveAll(secondHalf);
        } catch (DataIntegrityViolationException ex) {
            // If saving the second half fails, process it recursively in smaller batches
            processNavsInPartitions(secondHalf, schemeId);
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
