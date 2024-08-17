package com.example.application.mfschemes.service;

import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.models.projection.FundDetailProjection;
import com.example.application.mfschemes.repository.MFSchemeRepository;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeService.class);

    private final MFSchemeRepository mFSchemeRepository;

    public MfSchemeService(MFSchemeRepository mFSchemeRepository) {
        this.mFSchemeRepository = mFSchemeRepository;
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

    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mFSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }
}
