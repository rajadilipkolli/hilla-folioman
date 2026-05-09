package com.app.folioman.mfschemes.domain;

import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import org.jspecify.annotations.Nullable;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

public interface MfSchemeDtoToEntityMapperHelper {

    @AfterMapping
    default void updateMFScheme(MFSchemeDTO mfSchemeDTO, @MappingTarget MfFundSchemeEntity mfScheme) {}

    MFSchemeTypeEntity findOrCreateMFSchemeTypeEntity(String type, String category, @Nullable String subCategory);
}
