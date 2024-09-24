package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.SchemeNotFoundException;
import com.app.folioman.mfschemes.entities.MFSchemeNav;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.SchemeNAVDataDtoToEntityMapper;
import com.app.folioman.mfschemes.models.response.NavResponse;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import com.app.folioman.mfschemes.util.SchemeConstants;
import com.app.folioman.shared.FundDetailProjection;
import com.app.folioman.shared.MFSchemeProjection;
import com.app.folioman.shared.MfSchemeService;
import java.net.URI;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Transactional(readOnly = true)
@Service
class MfSchemeServiceDelegate implements MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeServiceDelegate.class);

    private final MfFundSchemeRepository mFSchemeRepository;
    private final SchemeNAVDataDtoToEntityMapper schemeNAVDataDtoToEntityMapper;
    private final RestClient restClient;
    private final TransactionTemplate transactionTemplate;

    MfSchemeServiceDelegate(
            MfFundSchemeRepository mFSchemeRepository,
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
    public void fetchSchemeDetails(Long schemeCode) {
        NavResponse navResponse = getNavResponseResponseEntity(schemeCode);
        processResponseEntity(schemeCode, navResponse);
    }

    @Override
    public Optional<MFSchemeProjection> fetchSchemesByRtaCode(String rtaCode) {
        return this.mFSchemeRepository.findByRtaCodeStartsWith(rtaCode);
    }

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
