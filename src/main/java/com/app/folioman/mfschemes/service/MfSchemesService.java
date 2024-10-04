package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.MFSchemeDTO;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.mapper.MfSchemeEntityToDtoMapper;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import com.app.folioman.shared.FundDetailProjection;
import com.app.folioman.shared.MFSchemeProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MfSchemesService {

    private final MfFundSchemeRepository mFSchemeRepository;
    private final MfSchemeEntityToDtoMapper mfSchemeEntityToDtoMapper;
    private final MfSchemeServiceDelegate mfSchemeServiceDelegate;

    MfSchemesService(
            MfFundSchemeRepository mFSchemeRepository,
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
    public List<MfFundScheme> saveAllEntities(List<MfFundScheme> list) {
        return mFSchemeRepository.saveAll(list);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MfFundScheme saveEntity(MfFundScheme mfScheme) {
        return mFSchemeRepository.save(mfScheme);
    }

    public Optional<MfFundScheme> findBySchemeCode(Long schemeCode) {
        return this.mFSchemeRepository.findByAmfiCode(schemeCode);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
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
