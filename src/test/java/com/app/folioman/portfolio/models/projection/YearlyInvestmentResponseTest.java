package com.app.folioman.portfolio.models.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearlyInvestmentResponseTest {

    @Test
    void testGetYear_ReturnsValidYear() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(2023);

        Integer result = response.getYear();

        assertEquals(2023, result);
    }

    @Test
    void testGetYear_ReturnsNull() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(null);

        Integer result = response.getYear();

        assertNull(result);
    }

    @Test
    void testGetYear_ReturnsZero() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(0);

        Integer result = response.getYear();

        assertEquals(0, result);
    }

    @Test
    void testGetYear_ReturnsNegativeYear() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(-1);

        Integer result = response.getYear();

        assertEquals(-1, result);
    }

    @Test
    void testGetYearlyInvestment_ReturnsValidAmount() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = new BigDecimal("1000.50");
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertEquals(expectedAmount, result);
    }

    @Test
    void testGetYearlyInvestment_ReturnsNull() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYearlyInvestment()).thenReturn(null);

        BigDecimal result = response.getYearlyInvestment();

        assertNull(result);
    }

    @Test
    void testGetYearlyInvestment_ReturnsZero() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = BigDecimal.ZERO;
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertEquals(expectedAmount, result);
    }

    @Test
    void testGetYearlyInvestment_ReturnsNegativeAmount() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = new BigDecimal("-500.25");
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertEquals(expectedAmount, result);
    }

    @Test
    void testGetYearlyInvestment_ReturnsLargeAmount() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = new BigDecimal("999999999.99");
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertEquals(expectedAmount, result);
    }

    @Test
    void testBothMethods_WorkTogether() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(2024);
        when(response.getYearlyInvestment()).thenReturn(new BigDecimal("5000.00"));

        Integer year = response.getYear();
        BigDecimal investment = response.getYearlyInvestment();

        assertEquals(2024, year);
        assertEquals(new BigDecimal("5000.00"), investment);
    }
}
