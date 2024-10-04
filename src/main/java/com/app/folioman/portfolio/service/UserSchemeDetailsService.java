package com.app.folioman.portfolio.service;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.portfolio.entities.UserSchemeDetails;
import com.app.folioman.portfolio.repository.UserSchemeDetailsRepository;
import com.app.folioman.shared.FundDetailProjection;
import com.app.folioman.shared.MFSchemeProjection;
import com.app.folioman.shared.MfSchemeService;
import com.app.folioman.shared.UserSchemeDetailService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
class UserSchemeDetailsService implements UserSchemeDetailService {

    private static final Logger log = LoggerFactory.getLogger(UserSchemeDetailsService.class);

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;
    private final MfSchemeService mfSchemeService;
    private final MFNavService mfNavService;
    private final TaskExecutor taskExecutor;

    UserSchemeDetailsService(
            UserSchemeDetailsRepository userSchemeDetailsRepository,
            MfSchemeService mfSchemeService,
            MFNavService mfNavService,
            @Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
        this.mfSchemeService = mfSchemeService;
        this.mfNavService = mfNavService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void setUserSchemeAMFIIfNull() {
        List<UserSchemeDetails> userSchemeDetailsEntities = userSchemeDetailsRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String rtaCode = userSchemeDetailsEntity.getRtaCode();
            if (StringUtils.hasText(rtaCode)) {
                log.debug(
                        "RTA code for userSchemeDetailsEntity with id: {} is {}",
                        userSchemeDetailsEntity.getId(),
                        rtaCode);
                List<MFSchemeProjection> mfSchemeEntityList =
                        mfSchemeService.fetchSchemesByRtaCode(rtaCode.substring(0, rtaCode.length() - 1));
                if (!mfSchemeEntityList.isEmpty()) {
                    Optional<MFSchemeProjection> matchingScheme = mfSchemeEntityList.stream()
                            .filter(scheme -> Objects.equals(scheme.getIsin(), userSchemeDetailsEntity.getIsin()))
                            .findFirst();

                    matchingScheme.ifPresentOrElse(
                            mfSchemeProjection -> updateUserSchemeDetails(
                                    userSchemeDetailsEntity.getId(),
                                    mfSchemeProjection.getAmfiCode(),
                                    mfSchemeProjection.getIsin()),
                            () -> {
                                log.debug("ISIN not found in the list of schemes");
                                MFSchemeProjection firstScheme = mfSchemeEntityList.getFirst();
                                updateUserSchemeDetails(
                                        userSchemeDetailsEntity.getId(),
                                        firstScheme.getAmfiCode(),
                                        firstScheme.getIsin());
                            });
                } else {
                    String scheme = userSchemeDetailsEntity.getScheme();
                    if (scheme == null) {
                        log.warn(
                                "Scheme is null for userSchemeDetailsEntity with id: {}",
                                userSchemeDetailsEntity.getId());
                    } else {
                        log.info("amfi is Null for scheme :{}", scheme);
                        // attempting to find ISIN
                        if (scheme.contains("ISIN:")) {
                            String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5)
                                    .strip();
                            if (StringUtils.hasText(isin)) {
                                Optional<MFSchemeProjection> mfSchemeEntity = mfSchemeService.findByPayOut(isin);
                                mfSchemeEntity.ifPresent(schemeEntity -> updateUserSchemeDetails(
                                        userSchemeDetailsEntity.getId(), schemeEntity.getAmfiCode(), isin));
                            } else {
                                log.warn("ISIN is null after extraction for scheme: {}", scheme);
                            }
                        } else {
                            // case where isin and amfi is null
                            List<FundDetailProjection> fundDetailProjections = mfSchemeService.fetchSchemes(scheme);
                            if (!fundDetailProjections.isEmpty()) {
                                Long schemeId = getSchemeId(fundDetailProjections, scheme);
                                if (null != schemeId) {
                                    updateUserSchemeDetails(userSchemeDetailsEntity.getId(), schemeId, null);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void loadHistoricalDataIfNotExists() {
        List<Long> historicalDataNotLoadedSchemeIdList = mfNavService.getHistoricalDataNotLoadedSchemeIdList();
        if (!historicalDataNotLoadedSchemeIdList.isEmpty()) {
            List<CompletableFuture<Void>> allSchemesWhereHistoricalDetailsNotLoadedCf =
                    historicalDataNotLoadedSchemeIdList.stream()
                            .map(schemeId -> CompletableFuture.runAsync(
                                    () -> mfSchemeService.fetchSchemeDetails(schemeId), taskExecutor))
                            .toList();
            CompletableFuture.allOf(allSchemesWhereHistoricalDetailsNotLoadedCf.toArray(new CompletableFuture<?>[0]))
                    .join();
            log.info("Completed loading HistoricalData for schemes that don't exist");
        }
    }

    public List<UserSchemeDetails> findBySchemesIn(List<UserSchemeDetails> userSchemeDetails) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetails);
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
        return (scheme.contains("Income")
                        && fundDetailProjection.getSchemeName().contains("IDCW"))
                || (!scheme.contains("Income")
                        && !fundDetailProjection.getSchemeName().contains("IDCW"));
    }
}
