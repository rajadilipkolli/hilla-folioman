package com.app.folioman.mfschemes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection for {@link com.app.folioman.mfschemes.entities.MFSchemeNav}.
 * Represents the Net Asset Value (NAV) data for a mutual fund scheme.
 *
 * @param nav      The Net Asset Value of the scheme.
 * @param navDate  The date for which the NAV is recorded.
 * @param amfiCode The AMFI (Association of Mutual Funds in India) code for the scheme.
 */
public record MFSchemeNavProjection(BigDecimal nav, LocalDate navDate, Long amfiCode) {}
