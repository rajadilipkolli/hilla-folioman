package com.app.folioman.mfschemes.rest.dtos;

/**
 * Projection for {@link com.app.folioman.mfschemes.domain.MfFundSchemeEntity}
 */
public interface MFSchemeProjection {
    Long getAmfiCode();

    String getIsin();
}
