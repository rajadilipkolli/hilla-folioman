package com.app.folioman.portfolio.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HarvestLot(
        LocalDate acquisitionDate, BigDecimal purchaseNav, BigDecimal originalUnits, BigDecimal remainingUnits) {
    public HarvestLot withRemainingUnits(BigDecimal newRemainingUnits) {
        return new HarvestLot(acquisitionDate, purchaseNav, originalUnits, newRemainingUnits);
    }
}
