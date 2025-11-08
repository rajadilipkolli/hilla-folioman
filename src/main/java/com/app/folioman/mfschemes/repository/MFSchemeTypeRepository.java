package com.app.folioman.mfschemes.repository;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeType, Integer> {

    @Cacheable(cacheNames = "findByTypeAndCategoryAndSubCategory", unless = "#result == null")
    MFSchemeType findByTypeAndCategoryAndSubCategory(String type, String category, String subCategory);
}
