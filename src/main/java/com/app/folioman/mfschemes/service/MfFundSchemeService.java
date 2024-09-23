package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfFundScheme;
import com.app.folioman.mfschemes.repository.MfFundSchemeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MfFundSchemeService {

    private final MfFundSchemeRepository mfFundSchemeRepository;

    public MfFundSchemeService(MfFundSchemeRepository mfFundSchemeRepository) {
        this.mfFundSchemeRepository = mfFundSchemeRepository;
    }

    @Transactional
    public void saveData(List<MfFundScheme> mfFundScheme) {
        this.mfFundSchemeRepository.saveAll(mfFundScheme);
    }

    public long getTotalCount() {
        return mfFundSchemeRepository.count();
    }

    public List<String> findDistinctIsin() {
        return mfFundSchemeRepository.findDistinctIsin();
    }
}
