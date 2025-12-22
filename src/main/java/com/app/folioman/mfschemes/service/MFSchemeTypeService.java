package com.app.folioman.mfschemes.service;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.repository.MFSchemeTypeRepository;
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

    public MFSchemeType findByTypeAndCategoryAndSubCategory(String type, String category, String subCategory) {
        return mfSchemeTypeRepository.findByTypeAndCategoryAndSubCategory(type, category, subCategory);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MFSchemeType saveCategory(MFSchemeType mfSchemeType) {
        return mfSchemeTypeRepository.save(mfSchemeType);
    }
}
