package com.example.application.mfschemes.service;

import com.example.application.mfschemes.entities.MFScheme;
import com.example.application.mfschemes.repository.MFSchemeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MfSchemeService {

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
}
