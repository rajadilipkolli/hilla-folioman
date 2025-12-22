package com.app.folioman.mfschemes;

import com.app.folioman.mfschemes.entities.MFSchemeType;
import com.app.folioman.mfschemes.entities.MfFundScheme;
import org.jspecify.annotations.Nullable;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

public interface MfSchemeDtoToEntityMapperHelper {

    @AfterMapping
    default void updateMFScheme(MFSchemeDTO mfSchemeDTO, @MappingTarget MfFundScheme mfScheme) {}

    MFSchemeType findOrCreateMFSchemeTypeEntity(String type, String category, @Nullable String subCategory);
}
