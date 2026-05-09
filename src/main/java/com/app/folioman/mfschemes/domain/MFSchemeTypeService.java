package com.app.folioman.mfschemes.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class MFSchemeTypeService {

    private final MFSchemeTypeRepository mfSchemeTypeRepository;

    MFSchemeTypeService(MFSchemeTypeRepository mfSchemeTypeRepository) {
        this.mfSchemeTypeRepository = mfSchemeTypeRepository;
    }

    public @Nullable MFSchemeTypeEntity findByTypeAndCategoryAndSubCategory(
            String type, String category, @Nullable String subCategory) {
        return mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MFSchemeTypeEntity saveCategory(MFSchemeTypeEntity mfSchemeTypeEntity) {
        return mfSchemeTypeRepository.save(mfSchemeTypeEntity);
    }
}
