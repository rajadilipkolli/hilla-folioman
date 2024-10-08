package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MfAmc;
import com.app.folioman.mfschemes.repository.MfAmcRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class MfAmcCacheService {

    private final MfAmcRepository mfAmcRepository;

    MfAmcCacheService(MfAmcRepository mfAmcRepository) {
        this.mfAmcRepository = mfAmcRepository;
    }

    @Cacheable(value = "findByAMCName", key = "#amcName", unless = "#result == null")
    public MfAmc findByName(String amcName) {
        return mfAmcRepository.findByNameIgnoreCase(amcName.toUpperCase(Locale.ENGLISH));
    }

    @Cacheable(value = "findByAMCCode", key = "#code", unless = "#result == null")
    public MfAmc findByCode(String code) {
        return this.mfAmcRepository.findByCode(code);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MfAmc saveMfAmc(MfAmc amc) {
        return mfAmcRepository.save(amc);
    }

    public List<MfAmc> findAllAmcs() {
        return this.mfAmcRepository.findAll();
    }
}
