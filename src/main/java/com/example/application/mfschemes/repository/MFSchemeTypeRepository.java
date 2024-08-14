package com.example.application.mfschemes.repository;

import com.example.application.mfschemes.entities.MFSchemeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MFSchemeTypeRepository extends JpaRepository<MFSchemeType, Integer> {

    MFSchemeType findByTypeAndCategoryAndSubCategory(String type, String category, String subCategory);
}
