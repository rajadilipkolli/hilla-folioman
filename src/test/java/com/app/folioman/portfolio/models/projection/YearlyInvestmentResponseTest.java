package com.app.folioman.portfolio.models.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearlyInvestmentResponseTest {

    @Test
    void getYearReturnsValidYear() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(2023);

        Integer result = response.getYear();

        assertThat(result).isEqualTo(2023);
    }

    @Test
    void getYearReturnsNull() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(null);

        Integer result = response.getYear();

        assertThat(result).isNull();
    }

    @Test
    void getYearReturnsZero() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(0);

        Integer result = response.getYear();

        assertThat(result).isZero();
    }

    @Test
    void getYearReturnsNegativeYear() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(-1);

        Integer result = response.getYear();

        assertThat(result).isEqualTo(-1);
    }

    @Test
    void getYearlyInvestmentReturnsValidAmount() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = new BigDecimal("1000.50");
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    void getYearlyInvestmentReturnsNull() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYearlyInvestment()).thenReturn(null);

        BigDecimal result = response.getYearlyInvestment();

        assertThat(result).isNull();
    }

    @Test
    void getYearlyInvestmentReturnsZero() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = BigDecimal.ZERO;
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    void getYearlyInvestmentReturnsNegativeAmount() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = new BigDecimal("-500.25");
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    void getYearlyInvestmentReturnsLargeAmount() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        BigDecimal expectedAmount = new BigDecimal("999999999.99");
        when(response.getYearlyInvestment()).thenReturn(expectedAmount);

        BigDecimal result = response.getYearlyInvestment();

        assertThat(result).isEqualTo(expectedAmount);
    }

    @Test
    void bothMethodsWorkTogether() {
        YearlyInvestmentResponse response = mock(YearlyInvestmentResponse.class);
        when(response.getYear()).thenReturn(2024);
        when(response.getYearlyInvestment()).thenReturn(new BigDecimal("5000.00"));

        Integer year = response.getYear();
        BigDecimal investment = response.getYearlyInvestment();

        assertThat(year).isEqualTo(2024);
        assertThat(investment).isEqualTo(new BigDecimal("5000.00"));
    }
}
