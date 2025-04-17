package com.app.folioman.mfschemes.models.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A lightweight projection for NAV date-value pairs.
 * Used for batch existence checks to avoid excessive database queries.
 *
 * @param nav The NAV value
 * @param navDate The date of the NAV
 */
public record NavDateValueProjection(BigDecimal nav, LocalDate navDate) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NavDateValueProjection that = (NavDateValueProjection) o;
        return Objects.equals(nav, that.nav) && Objects.equals(navDate, that.navDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nav, navDate);
    }
}
