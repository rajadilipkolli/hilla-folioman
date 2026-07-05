package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.domain.models.HarvestLot;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class HarvestLotTracker {

    private final Deque<HarvestLot> lots = new LinkedList<>();

    public void addTransaction(UserTransactionDetailsEntity txn) {
        if (txn.getAmount() == null
                || txn.getTransactionDate() == null
                || "STT_TAX".equals(txn.getType().name())) {
            return;
        }

        BigDecimal quantity = txn.getUnits() != null ? BigDecimal.valueOf(txn.getUnits()) : BigDecimal.ZERO;
        BigDecimal nav = txn.getNav() != null ? BigDecimal.valueOf(txn.getNav()) : BigDecimal.ZERO;

        if (txn.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            buy(txn.getTransactionDate(), quantity, nav);
        } else if (txn.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            sell(quantity);
        }
    }

    private void buy(java.time.LocalDate date, BigDecimal quantity, BigDecimal nav) {
        lots.addLast(new HarvestLot(date, nav, quantity, quantity));
    }

    private void sell(BigDecimal quantity) {
        BigDecimal pendingUnits = quantity.abs();

        while (pendingUnits.compareTo(BigDecimal.ZERO) > 0 && !lots.isEmpty()) {
            HarvestLot currentLot = lots.removeFirst();
            BigDecimal available = currentLot.remainingUnits();

            if (available.compareTo(pendingUnits) <= 0) {
                // Consume entire lot
                pendingUnits = pendingUnits.subtract(available);
            } else {
                // Consume partial lot, put remainder back at front
                BigDecimal remainder = available.subtract(pendingUnits);
                lots.addFirst(currentLot.withRemainingUnits(remainder));
                pendingUnits = BigDecimal.ZERO;
            }
        }
    }

    public List<HarvestLot> getOpenLots() {
        return new ArrayList<>(lots);
    }
}
