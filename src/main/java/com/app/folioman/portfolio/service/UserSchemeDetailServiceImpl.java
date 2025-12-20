package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.FundDetailProjection;
import com.app.folioman.mfschemes.MFSchemeProjection;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.portfolio.UserSchemeDetailService;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.repository.UserSchemeDetailsRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        List<UserSchemeDetails> userSchemeDetailsEntities = userSchemeDetailsRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(this::processUserSchemeDetails);
    }

    public List<UserSchemeDetails> findBySchemesIn(List<UserSchemeDetails> userSchemeDetails) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetails);
    }

    private void processUserSchemeDetails(UserSchemeDetails userSchemeDetails) {
        String rtaCode = userSchemeDetails.getRtaCode();
        if (StringUtils.hasText(rtaCode) && rtaCode.length() > 1) {
            String trimmedRtaCode = rtaCode.substring(0, rtaCode.length() - 1);
            processRtaCode(userSchemeDetails, trimmedRtaCode);
        } else {
            LOGGER.warn("rtaCode is too short: {}", rtaCode);
        }
    }

    private void processRtaCode(UserSchemeDetails userSchemeDetails, String rtaCode) {
        LOGGER.debug("RTA code for userSchemeDetailsEntity with id: {} is {}", userSchemeDetails.getId(), rtaCode);
        List<MFSchemeProjection> mfSchemeEntityList = mfSchemeService.fetchSchemesByRtaCode(rtaCode);
        if (!mfSchemeEntityList.isEmpty()) {
            handleNonEmptyMfSchemeList(userSchemeDetails, mfSchemeEntityList);
        } else {
            handleEmptyMfSchemeList(userSchemeDetails);
        }
    }

    private void handleEmptyMfSchemeList(UserSchemeDetails userSchemeDetails) {
        String scheme = userSchemeDetails.getScheme();
        if (scheme == null) {
            LOGGER.warn("Scheme is null for userSchemeDetailsEntity with id: {}", userSchemeDetails.getId());
        } else {
            LOGGER.info("AMFI is null for scheme: {}", scheme);
            if (scheme.contains("ISIN:")) {
                extractAndProcessIsin(userSchemeDetails, scheme);
            } else {
                processSchemeWithoutIsin(userSchemeDetails, scheme);
            }
        }
    }

    private void processSchemeWithoutIsin(UserSchemeDetails userSchemeDetails, String scheme) {
        List<FundDetailProjection> fundDetailProjections = mfSchemeService.fetchSchemes(scheme);
        if (!fundDetailProjections.isEmpty()) {
            Long schemeId = getSchemeId(fundDetailProjections, scheme);
            if (schemeId != null) {
                updateUserSchemeDetails(userSchemeDetails.getId(), schemeId, null);
            }
        }
    }

    private void extractAndProcessIsin(UserSchemeDetails userSchemeDetails, String scheme) {
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
            UserSchemeDetails userSchemeDetails, List<MFSchemeProjection> mfSchemeEntityList) {
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

    private void updateUserSchemeDetails(Long userSchemeId, Long schemeId, String isin) {
        userSchemeDetailsRepository.updateAmfiAndIsinById(schemeId, isin, userSchemeId);
    }

    private Long getSchemeId(List<FundDetailProjection> fundDetailProjections, String scheme) {
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
