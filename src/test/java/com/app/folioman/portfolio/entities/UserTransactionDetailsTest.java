package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.portfolio.models.request.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTransactionDetailsTest {

    private UserTransactionDetails userTransactionDetails;

    @BeforeEach
    void setUp() {
        userTransactionDetails = new UserTransactionDetails();
    }

    @Test
    void getAndSetId() {
        Long id = 1L;
        UserTransactionDetails result = userTransactionDetails.setId(id);

        assertThat(userTransactionDetails.getId()).isEqualTo(id);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetIdWithNull() {
        UserTransactionDetails result = userTransactionDetails.setId(null);

        assertThat(userTransactionDetails.getId()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetTransactionDate() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        UserTransactionDetails result = userTransactionDetails.setTransactionDate(date);

        assertThat(userTransactionDetails.getTransactionDate()).isEqualTo(date);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetTransactionDateWithNull() {
        UserTransactionDetails result = userTransactionDetails.setTransactionDate(null);

        assertThat(userTransactionDetails.getTransactionDate()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetDescription() {
        String description = "Test Description";
        UserTransactionDetails result = userTransactionDetails.setDescription(description);

        assertThat(userTransactionDetails.getDescription()).isEqualTo(description);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetDescriptionWithNull() {
        UserTransactionDetails result = userTransactionDetails.setDescription(null);

        assertThat(userTransactionDetails.getDescription()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetAmount() {
        BigDecimal amount = new BigDecimal("100.50");
        UserTransactionDetails result = userTransactionDetails.setAmount(amount);

        assertThat(userTransactionDetails.getAmount()).isEqualTo(amount);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetAmountWithNull() {
        UserTransactionDetails result = userTransactionDetails.setAmount(null);

        assertThat(userTransactionDetails.getAmount()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetUnits() {
        Double units = 10.5;
        UserTransactionDetails result = userTransactionDetails.setUnits(units);

        assertThat(userTransactionDetails.getUnits()).isEqualTo(units);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetUnitsWithNull() {
        UserTransactionDetails result = userTransactionDetails.setUnits(null);

        assertThat(userTransactionDetails.getUnits()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetNav() {
        Double nav = 25.75;
        UserTransactionDetails result = userTransactionDetails.setNav(nav);

        assertThat(userTransactionDetails.getNav()).isEqualTo(nav);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetNavWithNull() {
        UserTransactionDetails result = userTransactionDetails.setNav(null);

        assertThat(userTransactionDetails.getNav()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetBalance() {
        Double balance = 1000.0;
        UserTransactionDetails result = userTransactionDetails.setBalance(balance);

        assertThat(userTransactionDetails.getBalance()).isEqualTo(balance);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetBalanceWithNull() {
        UserTransactionDetails result = userTransactionDetails.setBalance(null);

        assertThat(userTransactionDetails.getBalance()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetType() {
        TransactionType type = TransactionType.PURCHASE;
        UserTransactionDetails result = userTransactionDetails.setType(type);

        assertThat(userTransactionDetails.getType()).isEqualTo(type);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetTypeWithNull() {
        UserTransactionDetails result = userTransactionDetails.setType(null);

        assertThat(userTransactionDetails.getType()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetDividendRate() {
        String dividendRate = "5.5%";
        UserTransactionDetails result = userTransactionDetails.setDividendRate(dividendRate);

        assertThat(userTransactionDetails.getDividendRate()).isEqualTo(dividendRate);
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetDividendRateWithNull() {
        UserTransactionDetails result = userTransactionDetails.setDividendRate(null);

        assertThat(userTransactionDetails.getDividendRate()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void getAndSetUserSchemeDetails() {
        UserSchemeDetails userSchemeDetails = new UserSchemeDetails();
        UserTransactionDetails result = this.userTransactionDetails.setUserSchemeDetails(userSchemeDetails);

        assertThat(this.userTransactionDetails.getUserSchemeDetails()).isEqualTo(userSchemeDetails);
        assertThat(result).isSameAs(this.userTransactionDetails);
    }

    @Test
    void getAndSetUserSchemeDetailsWithNull() {
        UserTransactionDetails result = userTransactionDetails.setUserSchemeDetails(null);

        assertThat(userTransactionDetails.getUserSchemeDetails()).isNull();
        assertThat(result).isSameAs(userTransactionDetails);
    }

    @Test
    void equalsWithSameReference() {
        assertThat(userTransactionDetails).isEqualTo(userTransactionDetails);
    }

    @Test
    void equalsWithNull() {
        assertThat(userTransactionDetails).isNotEqualTo(null);
    }

    @Test
    void equalsWithDifferentClass() {
        assertThat(userTransactionDetails).isNotEqualTo("different class");
    }

    @Test
    void equalsWithNullId() {
        UserTransactionDetails other = new UserTransactionDetails();
        assertThat(other).isNotEqualTo(userTransactionDetails);
    }

    @Test
    void equalsWithSameId() {
        Long id = 1L;
        userTransactionDetails.setId(id);
        UserTransactionDetails other = new UserTransactionDetails().setId(id);

        assertThat(other).isEqualTo(userTransactionDetails);
    }

    @Test
    void equalsWithDifferentId() {
        userTransactionDetails.setId(1L);
        UserTransactionDetails other = new UserTransactionDetails().setId(2L);

        assertThat(other).isNotEqualTo(userTransactionDetails);
    }

    @Test
    void equalsWithOneNullId() {
        userTransactionDetails.setId(1L);
        UserTransactionDetails other = new UserTransactionDetails();

        assertThat(other).isNotEqualTo(userTransactionDetails);
    }

    @Test
    void testHashCode() {
        int hashCode1 = userTransactionDetails.hashCode();
        int hashCode2 = userTransactionDetails.hashCode();

        assertThat(hashCode2).isEqualTo(hashCode1);
    }

    @Test
    void hashCodeConsistency() {
        UserTransactionDetails other = new UserTransactionDetails();

        assertThat(other).hasSameHashCodeAs(userTransactionDetails);
    }

    @Test
    void methodChaining() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        BigDecimal amount = new BigDecimal("100.00");

        UserTransactionDetails result = userTransactionDetails
                .setId(1L)
                .setTransactionDate(date)
                .setDescription("Test")
                .setAmount(amount)
                .setUnits(10.0)
                .setNav(10.0)
                .setBalance(100.0)
                .setType(TransactionType.PURCHASE)
                .setDividendRate("5%");

        assertThat(result).isSameAs(userTransactionDetails);
        assertThat(userTransactionDetails.getId()).isOne();
        assertThat(userTransactionDetails.getTransactionDate()).isEqualTo(date);
        assertThat(userTransactionDetails.getDescription()).isEqualTo("Test");
        assertThat(userTransactionDetails.getAmount()).isEqualTo(amount);
        assertThat(userTransactionDetails.getUnits()).isEqualTo(10.0);
        assertThat(userTransactionDetails.getNav()).isEqualTo(10.0);
        assertThat(userTransactionDetails.getBalance()).isEqualTo(100.0);
        assertThat(userTransactionDetails.getType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(userTransactionDetails.getDividendRate()).isEqualTo("5%");
    }
}
