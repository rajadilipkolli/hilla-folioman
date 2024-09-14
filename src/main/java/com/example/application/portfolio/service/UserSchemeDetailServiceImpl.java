package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.repository.UserSchemeDetailsRepository;
import com.example.application.shared.FundDetailProjection;
import com.example.application.shared.MFSchemeProjection;
import com.example.application.shared.MfSchemeService;
import com.example.application.shared.UserSchemeDetailsService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class UserSchemeDetailServiceImpl implements UserSchemeDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserSchemeDetailServiceImpl.class);

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;
    private final MfSchemeService mfSchemeService;

    public UserSchemeDetailServiceImpl(
            UserSchemeDetailsRepository userSchemeDetailsRepository, MfSchemeService mfSchemeService) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
        this.mfSchemeService = mfSchemeService;
    }

    public List<UserSchemeDetails> findBySchemesIn(List<UserSchemeDetails> userSchemeDetails) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetails);
    }

    @Override
    public void setAMFIIfNull() {
        List<UserSchemeDetails> userSchemeDetailsEntities = userSchemeDetailsRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String scheme = userSchemeDetailsEntity.getScheme();
            if (scheme == null) {
                log.warn("Scheme is null for userSchemeDetailsEntity with id: {}", userSchemeDetailsEntity.getId());
            }
            log.info("amfi is Null for scheme :{}", scheme);
            // attempting to find ISIN
            if (scheme.contains("ISIN:")) {
                String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
                if (StringUtils.hasText(isin)) {
                    Optional<MFSchemeProjection> mfSchemeEntity = mfSchemeService.findByPayOut(isin);
                    mfSchemeEntity.ifPresent(schemeEntity ->
                            updateUserSchemeDetails(userSchemeDetailsEntity.getId(), schemeEntity.getSchemeId(), isin));
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
        });
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
