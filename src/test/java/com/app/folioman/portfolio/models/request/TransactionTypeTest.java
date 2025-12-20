package com.app.folioman.portfolio.models.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransactionTypeTest {

    @Test
    void shouldHaveCorrectValues() {
        // Check that enum has the expected values
        assertThat(TransactionType.values()).hasSize(15);
        assertThat(TransactionType.valueOf("PURCHASE")).isEqualTo(TransactionType.PURCHASE);
        assertThat(TransactionType.valueOf("PURCHASE_SIP")).isEqualTo(TransactionType.PURCHASE_SIP);
        assertThat(TransactionType.valueOf("REDEMPTION")).isEqualTo(TransactionType.REDEMPTION);
        assertThat(TransactionType.valueOf("SWITCH_IN")).isEqualTo(TransactionType.SWITCH_IN);
        assertThat(TransactionType.valueOf("SWITCH_IN_MERGER")).isEqualTo(TransactionType.SWITCH_IN_MERGER);
        assertThat(TransactionType.valueOf("SWITCH_OUT")).isEqualTo(TransactionType.SWITCH_OUT);
        assertThat(TransactionType.valueOf("SWITCH_OUT_MERGER")).isEqualTo(TransactionType.SWITCH_OUT_MERGER);
        assertThat(TransactionType.valueOf("DIVIDEND_PAYOUT")).isEqualTo(TransactionType.DIVIDEND_PAYOUT);
        assertThat(TransactionType.valueOf("DIVIDEND_REINVESTMENT")).isEqualTo(TransactionType.DIVIDEND_REINVESTMENT);
        assertThat(TransactionType.valueOf("SEGREGATION")).isEqualTo(TransactionType.SEGREGATION);
        assertThat(TransactionType.valueOf("STAMP_DUTY_TAX")).isEqualTo(TransactionType.STAMP_DUTY_TAX);
        assertThat(TransactionType.valueOf("TDS_TAX")).isEqualTo(TransactionType.TDS_TAX);
        assertThat(TransactionType.valueOf("STT_TAX")).isEqualTo(TransactionType.STT_TAX);
        assertThat(TransactionType.valueOf("REVERSAL")).isEqualTo(TransactionType.REVERSAL);
        assertThat(TransactionType.valueOf("MISC")).isEqualTo(TransactionType.MISC);
    }
}
