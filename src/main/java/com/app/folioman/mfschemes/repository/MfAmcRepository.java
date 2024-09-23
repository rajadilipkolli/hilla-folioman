package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.entities.MfAmc;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfAmcRepository extends JpaRepository<MfAmc, Long> {

    @Cacheable(value = "findByAMCCode", unless = "#result == null")
    MfAmc findByCode(String amcCode);
}
