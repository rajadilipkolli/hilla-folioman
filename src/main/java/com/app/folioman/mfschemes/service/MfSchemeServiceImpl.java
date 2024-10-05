package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.SchemeNotFoundException;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.app.folioman.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.app.folioman.mfschemes.models.response.NavResponse;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import com.app.folioman.mfschemes.util.SchemeConstants;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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

    public MfSchemeServiceImpl(
            RestClient restClient,
            MfFundSchemeRepository mFSchemeRepository,
            MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper,
            SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper,
            PlatformTransactionManager transactionManager) {
        this.restClient = restClient;
        this.mFSchemeRepository = mFSchemeRepository;
        this.mfSchemeEntityToDtoMapper = mfSchemeEntityToDtoMapper;
        this.schemeNAVDataDtoToEntityMapper = schemeNAVDataDtoToEntityMapper;
        // Create a new TransactionTemplate with the desired propagation behavior
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
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
        return this.mFSchemeRepository.findByAmfiCode(schemeCode);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        return this.mFSchemeRepository
                .findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, navDate)
                .map(mfSchemeEntityToDtoMapper::convertEntityToDto);
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
        Optional<MfFundScheme> entityBySchemeId = this.mFSchemeRepository.findByAmfiCode(schemeCode);
        if (entityBySchemeId.isEmpty()) {
            // Scenario where scheme is discontinued or merged with other
            LOGGER.error("Found Discontinued SchemeCode : {}", schemeCode);
        } else {
            mergeList(navResponse, entityBySchemeId.get(), schemeCode);
        }
    }

    private void mergeList(NavResponse navResponse, MfFundScheme mfFundScheme, Long schemeCode) {
        if (navResponse.data().size() != mfFundScheme.getMfSchemeNavs().size()) {
            List<MFSchemeNav> navList = navResponse.data().stream()
                    .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                    .map(schemeNAVDataDtoToEntityMapper::schemeNAVDataDTOToEntity)
                    .toList();
            LOGGER.info("No of entries from Server :{} for schemeCode/amfi :{}", navList.size(), schemeCode);
            List<MFSchemeNav> newNavs = navList.stream()
                    .filter(nav -> !mfFundScheme.getMfSchemeNavs().contains(nav))
                    .toList();

            LOGGER.info("No of entities to insert :{} for schemeCode/amfi :{}", newNavs.size(), schemeCode);

            if (!newNavs.isEmpty()) {
                for (MFSchemeNav newSchemeNav : newNavs) {
                    mfFundScheme.addSchemeNav(newSchemeNav);
                }
                try {
                    transactionTemplate.execute(status -> this.mFSchemeRepository.save(mfFundScheme));
                } catch (ConstraintViolationException | DataIntegrityViolationException exception) {
                    LOGGER.error("ConstraintViolationException or DataIntegrityViolationException ", exception);
                }
            }
        } else {
            LOGGER.info("data in db and from service is same hence ignoring");
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
        LOGGER.info("Fetching SchemeDetails for AMFISchemeCode :{} ", schemeCode);
        return UriComponentsBuilder.fromUriString(SchemeConstants.MFAPI_WEBSITE_BASE_URL + schemeCode)
                .build()
                .toUri();
    }
}
