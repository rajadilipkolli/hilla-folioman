package com.app.folioman.portfolio.models.projection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlyInvestmentResponseTest {

    @Mock
    private MonthlyInvestmentResponse monthlyInvestmentResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getYear() {
        when(monthlyInvestmentResponse.getYear()).thenReturn(2023);
        assertThat(monthlyInvestmentResponse.getYear()).isEqualTo(2023);
    }

    @Test
    void getYearReturnsNull() {
        when(monthlyInvestmentResponse.getYear()).thenReturn(null);
        assertThat(monthlyInvestmentResponse.getYear()).isNull();
    }

    @Test
    void getMonthNumber() {
        when(monthlyInvestmentResponse.getMonthNumber()).thenReturn(12);
        assertThat(monthlyInvestmentResponse.getMonthNumber()).isEqualTo(12);
    }

    @Test
    void getMonthNumberReturnsNull() {
        when(monthlyInvestmentResponse.getMonthNumber()).thenReturn(null);
        assertThat(monthlyInvestmentResponse.getMonthNumber()).isNull();
    }

    @Test
    void getInvestmentPerMonth() {
        BigDecimal investment = new BigDecimal("1000.50");
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(investment);
        assertThat(monthlyInvestmentResponse.getInvestmentPerMonth()).isEqualTo(investment);
    }

    @Test
    void getInvestmentPerMonthReturnsNull() {
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(null);
        assertThat(monthlyInvestmentResponse.getInvestmentPerMonth()).isNull();
    }

    @Test
    void getInvestmentPerMonthWithZero() {
        BigDecimal zero = BigDecimal.ZERO;
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(zero);
        assertThat(monthlyInvestmentResponse.getInvestmentPerMonth()).isEqualTo(zero);
    }

    @Test
    void getInvestmentPerMonthWithNegativeValue() {
        BigDecimal negative = new BigDecimal("-500.25");
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(negative);
        assertThat(monthlyInvestmentResponse.getInvestmentPerMonth()).isEqualTo(negative);
    }

    @Test
    void getCumulativeInvestment() {
        BigDecimal cumulative = new BigDecimal("12000.75");
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(cumulative);
        assertThat(monthlyInvestmentResponse.getCumulativeInvestment()).isEqualTo(cumulative);
    }

    @Test
    void getCumulativeInvestmentReturnsNull() {
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(null);
        assertThat(monthlyInvestmentResponse.getCumulativeInvestment()).isNull();
    }

    @Test
    void getCumulativeInvestmentWithZero() {
        BigDecimal zero = BigDecimal.ZERO;
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(zero);
        assertThat(monthlyInvestmentResponse.getCumulativeInvestment()).isEqualTo(zero);
    }

    @Test
    void getCumulativeInvestmentWithNegativeValue() {
        BigDecimal negative = new BigDecimal("-1500.50");
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(negative);
        assertThat(monthlyInvestmentResponse.getCumulativeInvestment()).isEqualTo(negative);
    }

    @Test
    void allMethodsWithValidData() {
        when(monthlyInvestmentResponse.getYear()).thenReturn(2023);
        when(monthlyInvestmentResponse.getMonthNumber()).thenReturn(6);
        when(monthlyInvestmentResponse.getInvestmentPerMonth()).thenReturn(new BigDecimal("2500.00"));
        when(monthlyInvestmentResponse.getCumulativeInvestment()).thenReturn(new BigDecimal("15000.00"));

        assertThat(monthlyInvestmentResponse.getYear()).isEqualTo(2023);
        assertThat(monthlyInvestmentResponse.getMonthNumber()).isEqualTo(6);
        assertThat(monthlyInvestmentResponse.getInvestmentPerMonth()).isEqualTo(new BigDecimal("2500.00"));
        assertThat(monthlyInvestmentResponse.getCumulativeInvestment()).isEqualTo(new BigDecimal("15000.00"));
    }

    @Test
    void interfaceContract() {
        MonthlyInvestmentResponse mockResponse = mock(MonthlyInvestmentResponse.class);

        when(mockResponse.getYear()).thenReturn(2024);
        when(mockResponse.getMonthNumber()).thenReturn(1);
        when(mockResponse.getInvestmentPerMonth()).thenReturn(BigDecimal.TEN);
        when(mockResponse.getCumulativeInvestment()).thenReturn(BigDecimal.ONE);

        assertThat(mockResponse.getYear()).isEqualTo(2024);
        assertThat(mockResponse.getMonthNumber()).isOne();
        assertThat(mockResponse.getInvestmentPerMonth()).isEqualTo(BigDecimal.TEN);
        assertThat(mockResponse.getCumulativeInvestment()).isEqualTo(BigDecimal.ONE);
    }
}
