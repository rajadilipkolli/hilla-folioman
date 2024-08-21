package com.example.application.mfschemes.service;

import com.example.application.mfschemes.MFSchemeDTO;
import com.example.application.mfschemes.SchemeNotFoundException;
import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.entities.MFSchemeNav;
import com.example.application.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.example.application.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.example.application.mfschemes.models.projection.FundDetailProjection;
import com.example.application.mfschemes.models.response.NavResponse;
import com.example.application.mfschemes.repository.MFSchemeRepository;
import com.example.application.mfschemes.util.SchemeConstants;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Transactional(readOnly = true)
@Service
public class MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeService.class);

    private final MFSchemeRepository mFSchemeRepository;
    private final MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper;
    private final SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper;
    private final RestClient restClient;
    private final TransactionTemplate transactionTemplate;

    public MfSchemeService(
            MFSchemeRepository mFSchemeRepository,
            MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper,
            SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper,
            RestClient restClient,
            TransactionTemplate transactionTemplate) {
        this.mFSchemeRepository = mFSchemeRepository;
        this.mfSchemeEntityToDtoMapper = mfSchemeEntityToDtoMapper;
        this.schemeNAVDataDtoToEntityMapper = schemeNAVDataDtoToEntityMapper;
        this.restClient = restClient;
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        this.transactionTemplate = transactionTemplate;
    }

    public long count() {
        return mFSchemeRepository.count();
    }

    public List<Long> findAllSchemeIds() {
        return mFSchemeRepository.findAllSchemeIds();
    }

    @Transactional
    public List<MFScheme> saveAllEntities(List<MFScheme> list) {
        return mFSchemeRepository.saveAll(list);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MFScheme saveEntity(MFScheme mfScheme) {
        return mFSchemeRepository.save(mfScheme);
    }

    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.strip().replaceAll("\\s", "").toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mFSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        return this.mFSchemeRepository
                .findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, navDate)
                .map(mfSchemeEntityToDtoMapper::convertEntityToDto);
    }

    public void fetchSchemeDetails(Long schemeCode) {
        NavResponse navResponse = getNavResponseResponseEntity(schemeCode);
        processResponseEntity(schemeCode, navResponse);
    }

    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        NavResponse navResponse = getNavResponseResponseEntity(Long.valueOf(oldSchemeCode));
        processResponseEntity(newSchemeCode, navResponse);
    }

    private void processResponseEntity(Long schemeCode, NavResponse navResponse) {
        Optional<MFScheme> entityBySchemeId = this.mFSchemeRepository.findBySchemeId(schemeCode);
        if (entityBySchemeId.isEmpty()) {
            // Scenario where scheme is discontinued or merged with other
            LOGGER.error("Found Discontinued SchemeCode : {}", schemeCode);
        } else {
            mergeList(navResponse, entityBySchemeId.get(), schemeCode);
        }
    }

    private void mergeList(NavResponse navResponse, MFScheme mfScheme, Long schemeCode) {
        if (navResponse.data().size() != mfScheme.getMfSchemeNavs().size()) {
            List<MFSchemeNav> navList = navResponse.data().stream()
                    .map(navDataDTO -> navDataDTO.withSchemeId(schemeCode))
                    .map(schemeNAVDataDtoToEntityMapper::schemeNAVDataDTOToEntity)
                    .toList();
            LOGGER.info("No of entries from Server :{} for schemeCode/amfi :{}", navList.size(), schemeCode);
            List<MFSchemeNav> newNavs = navList.stream()
                    .filter(nav -> !mfScheme.getMfSchemeNavs().contains(nav))
                    .toList();

            LOGGER.info("No of entities to insert :{} for schemeCode/amfi :{}", newNavs.size(), schemeCode);

            if (!newNavs.isEmpty()) {
                for (MFSchemeNav newSchemeNav : newNavs) {
                    mfScheme.addSchemeNav(newSchemeNav);
                }
                try {
                    transactionTemplate.execute(status -> this.mFSchemeRepository.save(mfScheme));
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

    public Optional<MFScheme> findBySchemeCode(Long schemeCode) {
        return this.mFSchemeRepository.findBySchemeId(schemeCode);
    }
}
