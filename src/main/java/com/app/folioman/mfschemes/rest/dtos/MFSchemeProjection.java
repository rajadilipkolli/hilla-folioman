package com.app.folioman.mfschemes.rest.dtos;

import org.jspecify.annotations.Nullable;

/**
 * Projection for {@link com.app.folioman.mfschemes.domain.MfFundSchemeEntity}
 */
public interface MFSchemeProjection {
    Long getAmfiCode();

    @Nullable
    String getIsin();

    @Nullable
    MFSchemeTypeProjection getMfSchemeTypeEntity();
}
