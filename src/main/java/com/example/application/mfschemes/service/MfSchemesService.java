package com.example.application.mfschemes.service;

import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.example.application.mfschemes.models.response.MFSchemeDTO;
import com.example.application.mfschemes.repository.MFSchemeRepository;
import com.example.application.shared.FundDetailProjection;
import com.example.application.shared.MFSchemeProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MfSchemesService {

    private final MFSchemeRepository mFSchemeRepository;
    private final MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper;
    private final MfSchemeServiceDelegate mfSchemeServiceDelegate;

    MfSchemesService(
            MFSchemeRepository mFSchemeRepository,
            MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper,
            MfSchemeServiceDelegate mfSchemeServiceDelegate) {
        this.mFSchemeRepository = mFSchemeRepository;
        this.mfSchemeEntityToDtoMapper = mfSchemeEntityToDtoMapper;
        this.mfSchemeServiceDelegate = mfSchemeServiceDelegate;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MFScheme saveEntity(MFScheme mfScheme) {
        return mFSchemeRepository.save(mfScheme);
    }

    public Optional<MFScheme> findBySchemeCode(Long schemeCode) {
        return this.mFSchemeRepository.findBySchemeId(schemeCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<MFSchemeDTO> getMfSchemeDTO(Long schemeCode, LocalDate navDate) {
        return this.mFSchemeRepository
                .findBySchemeIdAndMfSchemeNavs_NavDate(schemeCode, navDate)
                .map(mfSchemeEntityToDtoMapper::convertEntityToDto);
    }

    public List<FundDetailProjection> fetchSchemes(String schemeName) {
        return mfSchemeServiceDelegate.fetchSchemes(schemeName);
    }

    public Optional<MFSchemeProjection> findByPayOut(String isin) {
        return mfSchemeServiceDelegate.findByPayOut(isin);
    }

    public void fetchSchemeDetails(Long schemeCode) {
        mfSchemeServiceDelegate.fetchSchemeDetails(schemeCode);
    }

    public void fetchSchemeDetails(String oldSchemeCode, Long newSchemeCode) {
        mfSchemeServiceDelegate.fetchSchemeDetails(oldSchemeCode, newSchemeCode);
    }
}
