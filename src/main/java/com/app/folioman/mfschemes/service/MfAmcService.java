package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.repository.MfAmcRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.commons.text.similarity.FuzzyScore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MfAmcService {

    private final MfAmcRepository mfAmcRepository;

    public MfAmcService(MfAmcRepository mfAmcRepository) {
        this.mfAmcRepository = mfAmcRepository;
    }

    @Cacheable(value = "findByAMCCode", key = "#code", unless = "#result == null")
    public MfAmc findByCode(String code) {
        return this.mfAmcRepository.findByCode(code);
    }

    @Transactional
    public MfAmc saveMfAmc(MfAmc amc) {
        return this.mfAmcRepository.save(amc);
    }

    @Cacheable(value = "findByAMCName", key = "#amcName", unless = "#result == null")
    public MfAmc findByName(String amcName) {
        MfAmc byNameIgnoreCase = mfAmcRepository.findByNameIgnoreCase(amcName.toUpperCase(Locale.ENGLISH));
        return byNameIgnoreCase != null ? byNameIgnoreCase : findClosestMatch(amcName);
    }

    private MfAmc findClosestMatch(String amcName) {
        List<MfAmc> mfAmcList = mfAmcRepository.findAll();
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);
        return mfAmcList.stream()
                .max(Comparator.comparingInt(entry -> fuzzyScore.fuzzyScore(amcName, entry.getName())))
                .orElse(null);
    }
}
