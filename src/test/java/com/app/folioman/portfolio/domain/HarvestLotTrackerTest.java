package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.portfolio.domain.models.HarvestLot;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HarvestLotTrackerTest {

    @Test
    void shouldTrackBuyLots() {
        HarvestLotTracker tracker = new HarvestLotTracker();

        UserTransactionDetailsEntity txn = new UserTransactionDetailsEntity();
        txn.setAmount(new BigDecimal("1000"));
        txn.setUnits(100.0);
        txn.setNav(10.0);
        txn.setTransactionDate(LocalDate.of(2023, 1, 15));
        txn.setType(TransactionType.PURCHASE_SIP);

        tracker.addTransaction(txn);

        List<HarvestLot> lots = tracker.getOpenLots();
        assertThat(lots).hasSize(1);
        assertThat(lots.get(0).remainingUnits()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(lots.get(0).acquisitionDate()).isEqualTo(LocalDate.of(2023, 1, 15));
    }

    @Test
    void shouldConsumeLotsOnSellFifo() {
        HarvestLotTracker tracker = new HarvestLotTracker();

        // Buy 1
        UserTransactionDetailsEntity buy1 = new UserTransactionDetailsEntity();
        buy1.setAmount(new BigDecimal("1000"));
        buy1.setUnits(100.0);
        buy1.setNav(10.0);
        buy1.setTransactionDate(LocalDate.of(2023, 1, 15));
        buy1.setType(TransactionType.PURCHASE_SIP);
        tracker.addTransaction(buy1);

        // Buy 2
        UserTransactionDetailsEntity buy2 = new UserTransactionDetailsEntity();
        buy2.setAmount(new BigDecimal("500"));
        buy2.setUnits(50.0);
        buy2.setNav(10.0);
        buy2.setTransactionDate(LocalDate.of(2023, 2, 15));
        buy2.setType(TransactionType.PURCHASE_SIP);
        tracker.addTransaction(buy2);

        // Sell 120 units
        UserTransactionDetailsEntity sell = new UserTransactionDetailsEntity();
        sell.setAmount(new BigDecimal("-1200"));
        sell.setUnits(-120.0);
        sell.setNav(10.0);
        sell.setTransactionDate(LocalDate.of(2023, 3, 15));
        sell.setType(TransactionType.REVERSAL);
        tracker.addTransaction(sell);

        List<HarvestLot> lots = tracker.getOpenLots();
        assertThat(lots).hasSize(1);
        // 100 units from buy1 consumed, 20 units from buy2 consumed. 30 units remaining in buy2.
        assertThat(lots.get(0).remainingUnits()).isEqualByComparingTo(new BigDecimal("30"));
        assertThat(lots.get(0).acquisitionDate()).isEqualTo(LocalDate.of(2023, 2, 15));
    }

    @Test
    void shouldIgnoreSttTaxTransactions() {
        HarvestLotTracker tracker = new HarvestLotTracker();

        UserTransactionDetailsEntity txn = new UserTransactionDetailsEntity();
        txn.setAmount(new BigDecimal("-10"));
        txn.setUnits(null);
        txn.setNav(null);
        txn.setTransactionDate(LocalDate.of(2023, 1, 15));
        txn.setType(TransactionType.STT_TAX);

        tracker.addTransaction(txn);

        assertThat(tracker.getOpenLots()).isEmpty();
    }
}
