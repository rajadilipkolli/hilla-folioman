package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.repository.MfAmcRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.text.similarity.FuzzyScore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MfAmcService {

    private final MfAmcRepository mfAmcRepository;

    private final MfAmcCacheService mfAmcCacheService;

    private final ReentrantLock reentrantLock = new ReentrantLock();

    public MfAmcService(MfAmcRepository mfAmcRepository, MfAmcCacheService mfAmcCacheService) {
        this.mfAmcRepository = mfAmcRepository;
        this.mfAmcCacheService = mfAmcCacheService;
    }

    @Cacheable(value = "findByAMCCode", key = "#code", unless = "#result == null")
    public MfAmc findByCode(String code) {
        return this.mfAmcRepository.findByCode(code);
    }

    public MfAmc saveMfAmc(MfAmc amc) {
        return this.mfAmcCacheService.saveMfAmc(amc);
    }

    public MfAmc findByName(String amcName) {
        MfAmc byNameIgnoreCase = mfAmcCacheService.findByName(amcName);
        return byNameIgnoreCase != null ? byNameIgnoreCase : findClosestMatch(amcName);
    }

    private MfAmc findClosestMatch(String amcName) {
        List<MfAmc> mfAmcList = mfAmcRepository.findAll();
        FuzzyScore fuzzyScore = new FuzzyScore(Locale.ENGLISH);
        return mfAmcList.stream()
                .max(Comparator.comparingInt(entry -> fuzzyScore.fuzzyScore(amcName, entry.getName())))
                .orElse(null);
    }

    public MfAmc findOrCreateByName(String amcName) {
        MfAmc amc = findByName(amcName);
        if (amc == null) {
            reentrantLock.lock();
            try {
                // Recheck cache after acquiring the lock
                amc = mfAmcCacheService.findByName(amcName);
                if (amc == null) {
                    amc = new MfAmc();
                    amc.setName(amcName);
                    amc.setCode(amcName);
                    amc = mfAmcCacheService.saveMfAmc(amc);
                }
            } finally {
                reentrantLock.unlock();
            }
        }
        return amc;
    }
}
