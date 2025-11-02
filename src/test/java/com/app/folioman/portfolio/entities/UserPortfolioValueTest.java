package com.app.folioman.portfolio.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPortfolioValueTest {

    private UserPortfolioValue userPortfolioValue;
    private UserCASDetails mockUserCasDetails;

    @BeforeEach
    void setUp() {
        userPortfolioValue = new UserPortfolioValue();
        mockUserCasDetails = new UserCASDetails();
        mockUserCasDetails.setCasTypeEnum(com.app.folioman.portfolio.entities.CasTypeEnum.DETAILED);
        mockUserCasDetails.setFileTypeEnum(com.app.folioman.portfolio.entities.FileTypeEnum.CAMS);
        com.app.folioman.portfolio.entities.InvestorInfo ii = new com.app.folioman.portfolio.entities.InvestorInfo();
        ii.setEmail("");
        ii.setName("");
        mockUserCasDetails.setInvestorInfo(ii);
    }

    @Test
    void testGetAndSetId() {
        Long id = 1L;

        UserPortfolioValue result = userPortfolioValue.setId(id);

        assertEquals(id, userPortfolioValue.getId());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetIdWithNull() {
        UserPortfolioValue result = userPortfolioValue.setId(null);

        assertNull(userPortfolioValue.getId());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetDate() {
        LocalDate date = LocalDate.of(2023, 12, 15);

        UserPortfolioValue result = userPortfolioValue.setDate(date);

        assertEquals(date, userPortfolioValue.getDate());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetDateWithNull() {
        UserPortfolioValue result = userPortfolioValue.setDate(null);

        assertNull(userPortfolioValue.getDate());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetInvested() {
        BigDecimal invested = new BigDecimal("10000.50");

        UserPortfolioValue result = userPortfolioValue.setInvested(invested);

        assertEquals(invested, userPortfolioValue.getInvested());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetInvestedWithNull() {
        UserPortfolioValue result = userPortfolioValue.setInvested(null);

        assertNull(userPortfolioValue.getInvested());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetValue() {
        BigDecimal value = new BigDecimal("12000.75");

        UserPortfolioValue result = userPortfolioValue.setValue(value);

        assertEquals(value, userPortfolioValue.getValue());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetValueWithNull() {
        UserPortfolioValue result = userPortfolioValue.setValue(null);

        assertNull(userPortfolioValue.getValue());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetXirr() {
        BigDecimal xirr = new BigDecimal("15.25");

        UserPortfolioValue result = userPortfolioValue.setXirr(xirr);

        assertEquals(xirr, userPortfolioValue.getXirr());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetXirrWithNull() {
        UserPortfolioValue result = userPortfolioValue.setXirr(null);

        assertNull(userPortfolioValue.getXirr());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetLiveXirr() {
        BigDecimal liveXirr = new BigDecimal("16.75");

        UserPortfolioValue result = userPortfolioValue.setLiveXirr(liveXirr);

        assertEquals(liveXirr, userPortfolioValue.getLiveXirr());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetLiveXirrWithNull() {
        UserPortfolioValue result = userPortfolioValue.setLiveXirr(null);

        assertNull(userPortfolioValue.getLiveXirr());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetUserCasDetails() {
        UserPortfolioValue result = userPortfolioValue.setUserCasDetails(mockUserCasDetails);

        assertEquals(mockUserCasDetails, userPortfolioValue.getUserCasDetails());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testGetAndSetUserCasDetailsWithNull() {
        UserPortfolioValue result = userPortfolioValue.setUserCasDetails(null);

        assertNull(userPortfolioValue.getUserCasDetails());
        assertSame(userPortfolioValue, result);
    }

    @Test
    void testMethodChaining() {
        Long id = 1L;
        LocalDate date = LocalDate.of(2023, 12, 15);
        BigDecimal invested = new BigDecimal("10000.00");
        BigDecimal value = new BigDecimal("12000.00");
        BigDecimal xirr = new BigDecimal("15.00");
        BigDecimal liveXirr = new BigDecimal("16.00");

        UserPortfolioValue result = userPortfolioValue
                .setId(id)
                .setDate(date)
                .setInvested(invested)
                .setValue(value)
                .setXirr(xirr)
                .setLiveXirr(liveXirr)
                .setUserCasDetails(mockUserCasDetails);

        assertSame(userPortfolioValue, result);
        assertEquals(id, userPortfolioValue.getId());
        assertEquals(date, userPortfolioValue.getDate());
        assertEquals(invested, userPortfolioValue.getInvested());
        assertEquals(value, userPortfolioValue.getValue());
        assertEquals(xirr, userPortfolioValue.getXirr());
        assertEquals(liveXirr, userPortfolioValue.getLiveXirr());
        assertEquals(mockUserCasDetails, userPortfolioValue.getUserCasDetails());
    }
}
