package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.repository.UserSchemeDetailsRepository;
import com.example.application.shared.FundDetailProjection;
import com.example.application.shared.MFSchemeProjection;
import com.example.application.shared.MfSchemeService;
import com.example.application.shared.UserSchemeDetailService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
class UserSchemeDetailsServiceDelegate implements UserSchemeDetailService {

    private static final Logger log = LoggerFactory.getLogger(UserSchemeDetailsServiceDelegate.class);

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;
    private final MfSchemeService mfSchemeService;

    UserSchemeDetailsServiceDelegate(
            UserSchemeDetailsRepository userSchemeDetailsRepository, MfSchemeService mfSchemeService) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
        this.mfSchemeService = mfSchemeService;
    }

    @Override
    public void setUserSchemeAMFIIfNull() {
        List<UserSchemeDetails> userSchemeDetailsEntities = userSchemeDetailsRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String scheme = userSchemeDetailsEntity.getScheme();
            if (scheme == null) {
                log.warn("Scheme is null for userSchemeDetailsEntity with id: {}", userSchemeDetailsEntity.getId());
            } else {
                log.info("amfi is Null for scheme :{}", scheme);
                // attempting to find ISIN
                if (scheme.contains("ISIN:")) {
                    String isin =
                            scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
                    if (StringUtils.hasText(isin)) {
                        Optional<MFSchemeProjection> mfSchemeEntity = mfSchemeService.findByPayOut(isin);
                        mfSchemeEntity.ifPresent(schemeEntity -> updateUserSchemeDetails(
                                userSchemeDetailsEntity.getId(), schemeEntity.getSchemeId(), isin));
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
        });
    }

    @Override
    public void loadHistoricalDataIfNotExists() {
        List<Long> historicalDataNotLoadedSchemeIdList =
                userSchemeDetailsRepository.getHistoricalDataNotLoadedSchemeIdList();
        if (!historicalDataNotLoadedSchemeIdList.isEmpty()) {
            List<CompletableFuture<Void>> allSchemesWhereHistoricalDetailsNotLoadedCf =
                    historicalDataNotLoadedSchemeIdList.stream()
                            .map(schemeId ->
                                    CompletableFuture.runAsync(() -> mfSchemeService.fetchSchemeDetails(schemeId)))
                            .toList();
            CompletableFuture.allOf(allSchemesWhereHistoricalDetailsNotLoadedCf.toArray(new CompletableFuture<?>[0]))
                    .join();
            log.info("Completed loading HistoricalData for schemes that don't exists");
        }
    }

    private void updateUserSchemeDetails(Long userSchemeId, Long schemeId, String isin) {
        userSchemeDetailsRepository.updateAmfiAndIsinById(schemeId, isin, userSchemeId);
    }

    private Long getSchemeId(List<FundDetailProjection> fundDetailProjections, String scheme) {
        return fundDetailProjections.stream()
                .filter(fundDetailProjection -> isMatchingScheme(scheme, fundDetailProjection))
                .map(FundDetailProjection::schemeId)
                .findFirst()
                .orElse(null);
    }

    private boolean isMatchingScheme(String scheme, FundDetailProjection fundDetailProjection) {
        return (scheme.contains("Income") && fundDetailProjection.schemeName().contains("IDCW"))
                || (!scheme.contains("Income")
                        && !fundDetailProjection.schemeName().contains("IDCW"));
    }
}
