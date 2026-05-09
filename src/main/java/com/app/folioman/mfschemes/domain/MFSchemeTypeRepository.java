package com.app.folioman.mfschemes.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MFSchemeTypeRepository extends JpaRepository<MFSchemeTypeEntity, Integer> {

    @Nullable
    @Cacheable(cacheNames = "findByTypeAndCategoryAndSubCategory", unless = "#result == null")
    MFSchemeTypeEntity findByTypeAndCategoryAndSubCategory(String type, String category, @Nullable String subCategory);
}
