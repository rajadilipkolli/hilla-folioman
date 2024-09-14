package com.example.application.portfolio.service;

import com.example.application.portfolio.entities.UserSchemeDetails;
import com.example.application.portfolio.repository.UserSchemeDetailsRepository;
import com.example.application.shared.FundDetailProjection;
import com.example.application.shared.MFSchemeProjection;
import com.example.application.shared.MfSchemeService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class UserSchemeDetailService {

    private static final Logger log = LoggerFactory.getLogger(UserSchemeDetailService.class);

    private final UserSchemeDetailsRepository userSchemeDetailsRepository;
    private final MfSchemeService mfSchemeService;

    public UserSchemeDetailService(
            UserSchemeDetailsRepository userSchemeDetailsRepository, MfSchemeService mfSchemeService) {
        this.userSchemeDetailsRepository = userSchemeDetailsRepository;
        this.mfSchemeService = mfSchemeService;
    }

    public List<UserSchemeDetails> findBySchemesIn(List<UserSchemeDetails> userSchemeDetails) {
        return userSchemeDetailsRepository.findByUserFolioDetails_SchemesIn(userSchemeDetails);
    }

    public void setAMFIIfNull() {
        List<UserSchemeDetails> userSchemeDetailsEntities = userSchemeDetailsRepository.findByAmfiIsNull();
        userSchemeDetailsEntities.forEach(userSchemeDetailsEntity -> {
            String scheme = userSchemeDetailsEntity.getScheme();
            log.info("amfi is Null for scheme :{}", scheme);
            // attempting to find ISIN
            if (scheme.contains("ISIN:")) {
                String isin = scheme.substring(scheme.lastIndexOf("ISIN:") + 5).strip();
                if (StringUtils.hasText(isin)) {
                    Optional<MFSchemeProjection> mfSchemeEntity = mfSchemeService.findByPayOut(isin);
                    mfSchemeEntity.ifPresent(schemeEntity -> userSchemeDetailsRepository.updateAmfiAndIsinById(
                            schemeEntity.getSchemeId(), isin, userSchemeDetailsEntity.getId()));
                }
            } else {
                // case where isin and amfi is null
                List<FundDetailProjection> fundDetailProjections = mfSchemeService.fetchSchemes(scheme);
                if (!fundDetailProjections.isEmpty()) {
                    Long schemeId = getSchemeId(fundDetailProjections, scheme);
                    if (null != schemeId) {
                        userSchemeDetailsRepository.updateAmfiAndIsinById(
                                schemeId, null, userSchemeDetailsEntity.getId());
                    }
                }
            }
        });
    }

    private Long getSchemeId(List<FundDetailProjection> fundDetailProjections, String scheme) {
        return fundDetailProjections.stream()
                .filter(fundDetailProjection -> (scheme.contains("Income")
                                && fundDetailProjection.schemeName().contains("IDCW"))
                        || (!scheme.contains("Income")
                                && !fundDetailProjection.schemeName().contains("IDCW")))
                .map(FundDetailProjection::schemeId)
                .findFirst()
                .orElse(null);
    }
}
