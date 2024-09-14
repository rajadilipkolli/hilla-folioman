package com.example.application.mfschemes.service;

import com.example.application.mfschemes.repository.MFSchemeRepository;
import com.example.application.shared.FundDetailProjection;
import com.example.application.shared.MFSchemeProjection;
import com.example.application.shared.MfSchemeService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MfSchemeServiceImpl implements MfSchemeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MfSchemeServiceImpl.class);

    private final MFSchemeRepository mFSchemeRepository;

    public MfSchemeServiceImpl(MFSchemeRepository mFSchemeRepository) {
        this.mFSchemeRepository = mFSchemeRepository;
    }

    @Override
    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        String sName = "%" + schemeName.strip().replaceAll("\\s", "").toUpperCase(Locale.ROOT) + "%";
        LOGGER.info("Fetching schemes with :{}", sName);
        return this.mFSchemeRepository.findBySchemeNameLikeIgnoreCaseOrderBySchemeIdAsc(sName);
    }

    @Override
    public Optional<MFSchemeProjection> findByPayOut(String isin) {
        return mFSchemeRepository.findByPayOut(isin);
    }
}
