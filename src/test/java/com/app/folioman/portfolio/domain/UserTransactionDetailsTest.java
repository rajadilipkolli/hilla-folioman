package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.portfolio.rest.dtos.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTransactionDetailsTest {

    private UserTransactionDetailsEntity UserTransactionDetailsEntity;

    @BeforeEach
    void setUp() {
        UserTransactionDetailsEntity = new UserTransactionDetailsEntity();
    }

    @Test
    void getAndSetId() {
        Long id = 1L;
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setId(id);

        assertThat(UserTransactionDetailsEntity.getId()).isEqualTo(id);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetIdWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setId(null);

        assertThat(UserTransactionDetailsEntity.getId()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetTransactionDate() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setTransactionDate(date);

        assertThat(UserTransactionDetailsEntity.getTransactionDate()).isEqualTo(date);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetTransactionDateWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setTransactionDate(null);

        assertThat(UserTransactionDetailsEntity.getTransactionDate()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetDescription() {
        String description = "Test Description";
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setDescription(description);

        assertThat(UserTransactionDetailsEntity.getDescription()).isEqualTo(description);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetDescriptionWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setDescription(null);

        assertThat(UserTransactionDetailsEntity.getDescription()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetAmount() {
        BigDecimal amount = new BigDecimal("100.50");
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setAmount(amount);

        assertThat(UserTransactionDetailsEntity.getAmount()).isEqualTo(amount);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetAmountWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setAmount(null);

        assertThat(UserTransactionDetailsEntity.getAmount()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetUnits() {
        Double units = 10.5;
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setUnits(units);

        assertThat(UserTransactionDetailsEntity.getUnits()).isEqualTo(units);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetUnitsWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setUnits(null);

        assertThat(UserTransactionDetailsEntity.getUnits()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetNav() {
        Double nav = 25.75;
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setNav(nav);

        assertThat(UserTransactionDetailsEntity.getNav()).isEqualTo(nav);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetNavWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setNav(null);

        assertThat(UserTransactionDetailsEntity.getNav()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetBalance() {
        Double balance = 1000.0;
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setBalance(balance);

        assertThat(UserTransactionDetailsEntity.getBalance()).isEqualTo(balance);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetBalanceWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setBalance(null);

        assertThat(UserTransactionDetailsEntity.getBalance()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetType() {
        TransactionType type = TransactionType.PURCHASE;
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setType(type);

        assertThat(UserTransactionDetailsEntity.getType()).isEqualTo(type);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetTypeWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setType(null);

        assertThat(UserTransactionDetailsEntity.getType()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetDividendRate() {
        String dividendRate = "5.5%";
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setDividendRate(dividendRate);

        assertThat(UserTransactionDetailsEntity.getDividendRate()).isEqualTo(dividendRate);
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetDividendRateWithNull() {
        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setDividendRate(null);

        assertThat(UserTransactionDetailsEntity.getDividendRate()).isNull();
        assertThat(result).isSameAs(UserTransactionDetailsEntity);
    }

    @Test
    void getAndSetUserSchemeDetails() {
        UserSchemeDetailsEntity userSchemeDetailsEntity = new UserSchemeDetailsEntity();
        UserTransactionDetailsEntity result =
                this.UserTransactionDetailsEntity.setUserSchemeDetails(userSchemeDetailsEntity);

        assertThat(this.UserTransactionDetailsEntity.getUserSchemeDetails()).isEqualTo(userSchemeDetailsEntity);
        assertThat(result).isSameAs(this.UserTransactionDetailsEntity);
    }

    @Test
    void equalsWithSameReference() {
        assertThat(UserTransactionDetailsEntity).isEqualTo(UserTransactionDetailsEntity);
    }

    @Test
    void equalsWithNull() {
        assertThat(UserTransactionDetailsEntity).isNotEqualTo(null);
    }

    @Test
    void equalsWithDifferentClass() {
        assertThat(UserTransactionDetailsEntity).isNotEqualTo("different class");
    }

    @Test
    void equalsWithNullId() {
        UserTransactionDetailsEntity other = new UserTransactionDetailsEntity();
        assertThat(other).isNotEqualTo(UserTransactionDetailsEntity);
    }

    @Test
    void equalsWithSameId() {
        Long id = 1L;
        UserTransactionDetailsEntity.setId(id);
        UserTransactionDetailsEntity other = new UserTransactionDetailsEntity().setId(id);

        assertThat(other).isEqualTo(UserTransactionDetailsEntity);
    }

    @Test
    void equalsWithDifferentId() {
        UserTransactionDetailsEntity.setId(1L);
        UserTransactionDetailsEntity other = new UserTransactionDetailsEntity().setId(2L);

        assertThat(other).isNotEqualTo(UserTransactionDetailsEntity);
    }

    @Test
    void equalsWithOneNullId() {
        UserTransactionDetailsEntity.setId(1L);
        UserTransactionDetailsEntity other = new UserTransactionDetailsEntity();

        assertThat(other).isNotEqualTo(UserTransactionDetailsEntity);
    }

    @Test
    void testHashCode() {
        int hashCode1 = UserTransactionDetailsEntity.hashCode();
        int hashCode2 = UserTransactionDetailsEntity.hashCode();

        assertThat(hashCode2).isEqualTo(hashCode1);
    }

    @Test
    void hashCodeConsistency() {
        UserTransactionDetailsEntity other = new UserTransactionDetailsEntity();

        assertThat(other).hasSameHashCodeAs(UserTransactionDetailsEntity);
    }

    @Test
    void methodChaining() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        BigDecimal amount = new BigDecimal("100.00");

        UserTransactionDetailsEntity result = UserTransactionDetailsEntity.setId(1L)
                .setTransactionDate(date)
                .setDescription("Test")
                .setAmount(amount)
                .setUnits(10.0)
                .setNav(10.0)
                .setBalance(100.0)
                .setType(TransactionType.PURCHASE)
                .setDividendRate("5%");

        assertThat(result).isSameAs(UserTransactionDetailsEntity);
        assertThat(UserTransactionDetailsEntity.getId()).isOne();
        assertThat(UserTransactionDetailsEntity.getTransactionDate()).isEqualTo(date);
        assertThat(UserTransactionDetailsEntity.getDescription()).isEqualTo("Test");
        assertThat(UserTransactionDetailsEntity.getAmount()).isEqualTo(amount);
        assertThat(UserTransactionDetailsEntity.getUnits()).isEqualTo(10.0);
        assertThat(UserTransactionDetailsEntity.getNav()).isEqualTo(10.0);
        assertThat(UserTransactionDetailsEntity.getBalance()).isEqualTo(100.0);
        assertThat(UserTransactionDetailsEntity.getType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(UserTransactionDetailsEntity.getDividendRate()).isEqualTo("5%");
    }
}
