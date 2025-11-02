package com.app.folioman.portfolio.entities;

import static org.junit.jupiter.api.Assertions.*;

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
    void testGetAndSetId() {
        Long id = 1L;
        UserTransactionDetails result = userTransactionDetails.setId(id);

        assertEquals(id, userTransactionDetails.getId());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetIdWithNull() {
        UserTransactionDetails result = userTransactionDetails.setId(null);

        assertNull(userTransactionDetails.getId());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetTransactionDate() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        UserTransactionDetails result = userTransactionDetails.setTransactionDate(date);

        assertEquals(date, userTransactionDetails.getTransactionDate());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetTransactionDateWithNull() {
        UserTransactionDetails result = userTransactionDetails.setTransactionDate(null);

        assertNull(userTransactionDetails.getTransactionDate());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetDescription() {
        String description = "Test Description";
        UserTransactionDetails result = userTransactionDetails.setDescription(description);

        assertEquals(description, userTransactionDetails.getDescription());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetDescriptionWithNull() {
        UserTransactionDetails result = userTransactionDetails.setDescription(null);

        assertNull(userTransactionDetails.getDescription());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetAmount() {
        BigDecimal amount = new BigDecimal("100.50");
        UserTransactionDetails result = userTransactionDetails.setAmount(amount);

        assertEquals(amount, userTransactionDetails.getAmount());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetAmountWithNull() {
        UserTransactionDetails result = userTransactionDetails.setAmount(null);

        assertNull(userTransactionDetails.getAmount());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetUnits() {
        Double units = 10.5;
        UserTransactionDetails result = userTransactionDetails.setUnits(units);

        assertEquals(units, userTransactionDetails.getUnits());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetUnitsWithNull() {
        UserTransactionDetails result = userTransactionDetails.setUnits(null);

        assertNull(userTransactionDetails.getUnits());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetNav() {
        Double nav = 25.75;
        UserTransactionDetails result = userTransactionDetails.setNav(nav);

        assertEquals(nav, userTransactionDetails.getNav());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetNavWithNull() {
        UserTransactionDetails result = userTransactionDetails.setNav(null);

        assertNull(userTransactionDetails.getNav());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetBalance() {
        Double balance = 1000.0;
        UserTransactionDetails result = userTransactionDetails.setBalance(balance);

        assertEquals(balance, userTransactionDetails.getBalance());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetBalanceWithNull() {
        UserTransactionDetails result = userTransactionDetails.setBalance(null);

        assertNull(userTransactionDetails.getBalance());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetType() {
        TransactionType type = TransactionType.PURCHASE;
        UserTransactionDetails result = userTransactionDetails.setType(type);

        assertEquals(type, userTransactionDetails.getType());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetTypeWithNull() {
        UserTransactionDetails result = userTransactionDetails.setType(null);

        assertNull(userTransactionDetails.getType());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetDividendRate() {
        String dividendRate = "5.5%";
        UserTransactionDetails result = userTransactionDetails.setDividendRate(dividendRate);

        assertEquals(dividendRate, userTransactionDetails.getDividendRate());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetDividendRateWithNull() {
        UserTransactionDetails result = userTransactionDetails.setDividendRate(null);

        assertNull(userTransactionDetails.getDividendRate());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testGetAndSetUserSchemeDetails() {
        UserSchemeDetails userSchemeDetails = new UserSchemeDetails();
        UserTransactionDetails result = this.userTransactionDetails.setUserSchemeDetails(userSchemeDetails);

        assertEquals(userSchemeDetails, this.userTransactionDetails.getUserSchemeDetails());
        assertSame(this.userTransactionDetails, result);
    }

    @Test
    void testGetAndSetUserSchemeDetailsWithNull() {
        UserTransactionDetails result = userTransactionDetails.setUserSchemeDetails(null);

        assertNull(userTransactionDetails.getUserSchemeDetails());
        assertSame(userTransactionDetails, result);
    }

    @Test
    void testEqualsWithSameReference() {
        assertTrue(userTransactionDetails.equals(userTransactionDetails));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(userTransactionDetails.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertFalse(userTransactionDetails.equals("different class"));
    }

    @Test
    void testEqualsWithNullId() {
        UserTransactionDetails other = new UserTransactionDetails();
        assertFalse(userTransactionDetails.equals(other));
    }

    @Test
    void testEqualsWithSameId() {
        Long id = 1L;
        userTransactionDetails.setId(id);
        UserTransactionDetails other = new UserTransactionDetails().setId(id);

        assertTrue(userTransactionDetails.equals(other));
    }

    @Test
    void testEqualsWithDifferentId() {
        userTransactionDetails.setId(1L);
        UserTransactionDetails other = new UserTransactionDetails().setId(2L);

        assertFalse(userTransactionDetails.equals(other));
    }

    @Test
    void testEqualsWithOneNullId() {
        userTransactionDetails.setId(1L);
        UserTransactionDetails other = new UserTransactionDetails();

        assertFalse(userTransactionDetails.equals(other));
    }

    @Test
    void testHashCode() {
        int hashCode1 = userTransactionDetails.hashCode();
        int hashCode2 = userTransactionDetails.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeConsistency() {
        UserTransactionDetails other = new UserTransactionDetails();

        assertEquals(userTransactionDetails.hashCode(), other.hashCode());
    }

    @Test
    void testMethodChaining() {
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

        assertSame(userTransactionDetails, result);
        assertEquals(1L, userTransactionDetails.getId());
        assertEquals(date, userTransactionDetails.getTransactionDate());
        assertEquals("Test", userTransactionDetails.getDescription());
        assertEquals(amount, userTransactionDetails.getAmount());
        assertEquals(10.0, userTransactionDetails.getUnits());
        assertEquals(10.0, userTransactionDetails.getNav());
        assertEquals(100.0, userTransactionDetails.getBalance());
        assertEquals(TransactionType.PURCHASE, userTransactionDetails.getType());
        assertEquals("5%", userTransactionDetails.getDividendRate());
    }
}
