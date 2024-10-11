package com.app.folioman.mfschemes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection for {@link com.app.folioman.mfschemes.entities.MFSchemeNav}
 */
public record MFSchemeNavProjection(BigDecimal nav, LocalDate navDate, Long amfiCode) {}
