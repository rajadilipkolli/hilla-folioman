package com.app.folioman.portfolio.models;

import com.app.folioman.portfolio.entities.UserTransactionDetails;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class FIFOUnits {

    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal invested = BigDecimal.ZERO;
    private BigDecimal pnl = BigDecimal.ZERO;
    private BigDecimal average = BigDecimal.ZERO;

    private static final BigDecimal BALANCE_THRESHOLD = new BigDecimal("0.01");

    private final Deque<TransactionRecord> transactions = new LinkedList<>(); // FIFO Queue

    public void addTransaction(UserTransactionDetails txn) {
        BigDecimal quantity =
                new BigDecimal(txn.getUnits() == null ? "0.000" : txn.getUnits().toString());
        BigDecimal nav =
                new BigDecimal(txn.getNav() == null ? "0.0000" : txn.getNav().toString());

        if (txn.getAmount() == null) {
            // Skip transactions with no amount (e.g., certain corporate actions)
            return;
        } else if (txn.getAmount().compareTo(BigDecimal.ZERO) > 0
                && !"STT_TAX".equals(txn.getType().name())) {
            buy(quantity, nav, txn.getAmount());
        } else if (txn.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            sell(quantity, nav);
        }
    }

    private void sell(BigDecimal quantity, BigDecimal nav) {
        BigDecimal originalQuantity = quantity.abs();
        BigDecimal pendingUnits = originalQuantity;
        BigDecimal costPrice = BigDecimal.ZERO;
        BigDecimal price = null;

        while (pendingUnits.compareTo(BigDecimal.ZERO) > 0) {
            try {
                TransactionRecord record = transactions.removeFirst(); // FIFO
                BigDecimal units = record.units();
                price = record.price();

                if (units.compareTo(pendingUnits) <= 0) {
                    costPrice = costPrice.add(units.multiply(price));
                } else {
                    costPrice = costPrice.add(pendingUnits.multiply(price));
                }
                pendingUnits = pendingUnits.subtract(units);

            } catch (NoSuchElementException e) {
                // Break if transactions are empty
                break;
            }
        }

        // Re-add remaining units to the FIFO queue if oversold
        if (pendingUnits.compareTo(BigDecimal.ZERO) < 0 && price != null) {
            transactions.addFirst(new TransactionRecord(pendingUnits.negate(), price));
        }

        invested = invested.subtract(costPrice.setScale(2, RoundingMode.HALF_UP));
        balance = balance.subtract(originalQuantity);
        pnl = pnl.add(originalQuantity.multiply(nav).subtract(costPrice).setScale(2, RoundingMode.HALF_UP));

        if (balance.abs().compareTo(BALANCE_THRESHOLD) > 0) {
            average = invested.divide(balance, 4, RoundingMode.HALF_UP);
        }
    }

    private void buy(BigDecimal quantity, BigDecimal nav, BigDecimal amount) {
        balance = balance.add(quantity);

        invested = invested.add(amount).setScale(2, RoundingMode.HALF_UP);

        if (balance.abs().compareTo(BALANCE_THRESHOLD) > 0) {
            average = invested.divide(balance, 4, RoundingMode.HALF_UP);
        }

        transactions.addLast(new TransactionRecord(quantity, nav)); // Add to FIFO queue
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getInvested() {
        return invested;
    }

    public BigDecimal getPnl() {
        return pnl;
    }

    public BigDecimal getAverage() {
        return average;
    }
}
