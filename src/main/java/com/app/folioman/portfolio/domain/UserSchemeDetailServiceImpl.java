package com.app.folioman.portfolio.domain;

import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.portfolio.UserSchemeDetailService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
class UserSchemeDetailServiceImpl implements UserSchemeDetailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSchemeDetailServiceImpl.class);

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;
    private final MfSchemeService mfSchemeService;

    UserSchemeDetailServiceImpl(
            UserSchemeDetailsRepository userSchemeDetailsRepository, MfSchemeService mfSchemeService) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
        this.mfSchemeService = mfSchemeService;
    }

    @Override
    public void setUserSchemeAMFIIfNull() {
        List<UserSchemeDetailsEntity> userSchemeDetailsEntities = userSchemeDetailsRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(this::processUserSchemeDetails);
    }

    public List<UserSchemeDetailsEntity> findBySchemesIn(List<UserSchemeDetailsEntity> userSchemeDetailsEntities) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetailsEntities);
    }

    private void processUserSchemeDetails(UserSchemeDetailsEntity userSchemeDetailsEntity) {
        String rtaCode = userSchemeDetailsEntity.getRtaCode();
        if (StringUtils.hasText(rtaCode) && rtaCode.length() > 1) {
            String trimmedRtaCode = rtaCode.substring(0, rtaCode.length() - 1);
            processRtaCode(userSchemeDetailsEntity, trimmedRtaCode);
        } else {
            LOGGER.warn("rtaCode is too short: {}", rtaCode);
        }
    }

    private void processRtaCode(UserSchemeDetailsEntity userSchemeDetailsEntity, String rtaCode) {
        LOGGER.debug(
                "RTA code for userSchemeDetailsEntity with id: {} is {}", userSchemeDetailsEntity.getId(), rtaCode);
        List<MFSchemeProjection> mfSchemeEntityList = mfSchemeService.fetchSchemesByRtaCode(rtaCode);
        if (!mfSchemeEntityList.isEmpty()) {
            handleNonEmptyMfSchemeList(userSchemeDetailsEntity, mfSchemeEntityList);
        } else {
            handleEmptyMfSchemeList(userSchemeDetailsEntity);
        }
    }

    private void handleEmptyMfSchemeList(UserSchemeDetailsEntity userSchemeDetailsEntity) {
        String scheme = userSchemeDetailsEntity.getScheme();
        LOGGER.info("AMFI is null for scheme: {}", scheme);
        if (scheme.contains("ISIN:")) {
            extractAndProcessIsin(userSchemeDetailsEntity, scheme);
        } else {
            processSchemeWithoutIsin(userSchemeDetailsEntity, scheme);
        }
    }

    private void processSchemeWithoutIsin(UserSchemeDetailsEntity userSchemeDetails, String scheme) {
        List<FundDetailProjection> fundDetailProjections = mfSchemeService.fetchSchemes(scheme);
        if (!fundDetailProjections.isEmpty()) {
            Long schemeId = getSchemeId(fundDetailProjections, scheme);
            if (schemeId != null) {
                updateUserSchemeDetails(userSchemeDetails.getId(), schemeId, null);
            }
        }
    }

    private void extractAndProcessIsin(UserSchemeDetailsEntity userSchemeDetails, String scheme) {
        String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
        if (StringUtils.hasText(isin)) {
            Optional<MFSchemeProjection> mfSchemeEntity = mfSchemeService.findByPayOut(isin);
            mfSchemeEntity.ifPresent(schemeEntity ->
                    updateUserSchemeDetails(userSchemeDetails.getId(), schemeEntity.getAmfiCode(), isin));
        } else {
            LOGGER.warn("ISIN is null after extraction for scheme: {}", scheme);
        }
    }

    private void handleNonEmptyMfSchemeList(
            UserSchemeDetailsEntity userSchemeDetails, List<MFSchemeProjection> mfSchemeEntityList) {
        Optional<MFSchemeProjection> matchingScheme = mfSchemeEntityList.stream()
                .filter(scheme -> Objects.equals(scheme.getIsin(), userSchemeDetails.getIsin()))
                .findFirst();

        matchingScheme.ifPresentOrElse(
                mfSchemeProjection -> updateUserSchemeDetails(
                        userSchemeDetails.getId(), mfSchemeProjection.getAmfiCode(), mfSchemeProjection.getIsin()),
                () -> {
                    LOGGER.debug("ISIN not found in the list of schemes");
                    MFSchemeProjection firstScheme = mfSchemeEntityList.getFirst();
                    updateUserSchemeDetails(
                            userSchemeDetails.getId(), firstScheme.getAmfiCode(), firstScheme.getIsin());
                });
    }

    private void updateUserSchemeDetails(Long userSchemeId, Long schemeId, @Nullable String isin) {
        userSchemeDetailsRepository.updateAmfiAndIsinById(schemeId, isin, userSchemeId);
    }

    private @Nullable Long getSchemeId(List<FundDetailProjection> fundDetailProjections, String scheme) {
        return fundDetailProjections.stream()
                .filter(fundDetailProjection -> isMatchingScheme(scheme, fundDetailProjection))
                .map(FundDetailProjection::getAmfiCode)
                .findFirst()
                .orElse(null);
    }

    private boolean isMatchingScheme(String scheme, FundDetailProjection fundDetailProjection) {
        return scheme.contains("Income") == fundDetailProjection.getSchemeName().contains("IDCW");
    }
}
