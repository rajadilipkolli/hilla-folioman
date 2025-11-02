package com.app.folioman.portfolio.models.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MonthlyInvestmentResponseTest {

    @Mock
    private MonthlyInvestmentResponse monthlyInvestmentResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetYear() {
        when(monthlyInvestmentResponse.getYear()).thenReturn(2023);
        assertEquals(2023, monthlyInvestmentResponse.getYear());
    }

    @Test
    void testGetYearReturnsNull() {
        when(monthlyInvestmentResponse.getYear()).thenReturn(null);
        assertNull(monthlyInvestmentResponse.getYear());
    }

    @Test
    void testGetMonthNumber() {
        when(monthlyInvestmentResponse.getMonthNumber()).thenReturn(12);
        assertEquals(12, monthlyInvestmentResponse.getMonthNumber());
    }

    @Test
    void testGetMonthNumberReturnsNull() {
        when(monthlyInvestmentResponse.getMonthNumber()).thenReturn(null);
        assertNull(monthlyInvestmentResponse.getMonthNumber());
    }

    @Test
    void testGetInvestmentPerMonth() {
        BigDecimal investment = new BigDecimal("1000.50");
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(investment);
        assertEquals(investment, monthlyInvestmentResponse.getInvestmentPerMonth());
    }

    @Test
    void testGetInvestmentPerMonthReturnsNull() {
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(null);
        assertNull(monthlyInvestmentResponse.getInvestmentPerMonth());
    }

    @Test
    void testGetInvestmentPerMonthWithZero() {
        BigDecimal zero = BigDecimal.ZERO;
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(zero);
        assertEquals(zero, monthlyInvestmentResponse.getInvestmentPerMonth());
    }

    @Test
    void testGetInvestmentPerMonthWithNegativeValue() {
        BigDecimal negative = new BigDecimal("-500.25");
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(negative);
        assertEquals(negative, monthlyInvestmentResponse.getInvestmentPerMonth());
    }

    @Test
    void testGetCumulativeInvestment() {
        BigDecimal cumulative = new BigDecimal("12000.75");
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(cumulative);
        assertEquals(cumulative, monthlyInvestmentResponse.getCumulativeInvestment());
    }

    @Test
    void testGetCumulativeInvestmentReturnsNull() {
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(null);
        assertNull(monthlyInvestmentResponse.getCumulativeInvestment());
    }

    @Test
    void testGetCumulativeInvestmentWithZero() {
        BigDecimal zero = BigDecimal.ZERO;
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(zero);
        assertEquals(zero, monthlyInvestmentResponse.getCumulativeInvestment());
    }

    @Test
    void testGetCumulativeInvestmentWithNegativeValue() {
        BigDecimal negative = new BigDecimal("-1500.50");
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(negative);
        assertEquals(negative, monthlyInvestmentResponse.getCumulativeInvestment());
    }

    @Test
    void testAllMethodsWithValidData() {
        when(monthlyInvestmentResponse.getYear()).thenReturn(2023);
        when(monthlyInvestmentResponse.getMonthNumber()).thenReturn(6);
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(new BigDecimal("2500.00"));
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(new BigDecimal("15000.00"));

        assertEquals(2023, monthlyInvestmentResponse.getYear());
        assertEquals(6, monthlyInvestmentResponse.getMonthNumber());
        assertEquals(new BigDecimal("2500.00"), monthlyInvestmentResponse.getInvestmentPerMonth());
        assertEquals(new BigDecimal("15000.00"), monthlyInvestmentResponse.getCumulativeInvestment());
    }

    @Test
    void testInterfaceContract() {
        MonthlyInvestmentResponse mockResponse = mock(MonthlyInvestmentResponse.class);

        when(mockResponse.getYear()).thenReturn(2024);
        when(mockResponse.getMonthNumber()).thenReturn(1);
        when(mockResponse.getInvestmentPerMonth()).thenReturn(BigDecimal.TEN);
        when(mockResponse.getCumulativeInvestment()).thenReturn(BigDecimal.ONE);

        assertEquals(2024, mockResponse.getYear());
        assertEquals(1, mockResponse.getMonthNumber());
        assertEquals(BigDecimal.TEN, mockResponse.getInvestmentPerMonth());
        assertEquals(BigDecimal.ONE, mockResponse.getCumulativeInvestment());
    }
}
