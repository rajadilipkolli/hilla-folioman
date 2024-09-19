package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.SchemeNotFoundException;
import com.app.folioman.mfschemes.entities.MFScheme;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.app.folioman.mfschemes.models.response.NavResponse;
import com.app.folioman.mfschemes.repository.MFSchemeRepository;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.FundDetailProjection;
import com.app.folioman.shared.MFSchemeProjection;
import com.app.folioman.shared.MfSchemeService;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Transactional(readOnly = true)
@Service
class MfSchemeServiceDelegate implements MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeServiceDelegate.class);

    private final MFSchemeRepository mFSchemeRepository;
    private final SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper;
    private final RestClient restClient;
    private final TransactionTemplate transactionTemplate;

    MfSchemeServiceDelegate(
            MFSchemeRepository mFSchemeRepository,
            SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper,
            RestClient restClient,
            TransactionTemplate transactionTemplate) {
        this.mFSchemeRepository = mFSchemeRepository;
        this.schemeNAVDataDtoToEntityMapper = schemeNAVDataDtoToEntityMapper;
        this.restClient = restClient;
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Optional<MFSchemeProjection> findByPayOut(String isin) {
        return mFSchemeRepository.findByPayOut(isin);
    }

    @Override
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.strip().replaceAll("\\s", "").toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mFSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }

    @Override
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
}