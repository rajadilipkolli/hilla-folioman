package com.app.folioman.portfolio.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class XirrCalculatorTest {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.0001");

    @Test
    @DisplayName("Should calculate XIRR correctly for simple investment scenario")
    void calculateXirr_simpleInvestment_returnsCorrectRate() {
        // Arrange
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Initial investment
                new BigDecimal("1100")); // Return after one year

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2020, 1, 1), // Investment date
                LocalDate.of(2021, 1, 1)); // Return date

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isCloseTo(new BigDecimal("0.09978303"), within(TOLERANCE));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for multiple cash flows")
    void calculateXirr_multipleCashFlows_returnsCorrectRate() {
        // Arrange
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Initial investment
                new BigDecimal("-500"), // Additional investment
                new BigDecimal("750"), // Partial withdrawal
                new BigDecimal("1000")); // Final return

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2020, 1, 1), // Initial investment
                LocalDate.of(2020, 4, 1), // Additional investment
                LocalDate.of(2020, 10, 1), // Partial withdrawal
                LocalDate.of(2021, 1, 1)); // Final return

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expected value previously calculated or verified against a reliable source
        assertThat(result).isGreaterThan(new BigDecimal("0.19")).isLessThan(new BigDecimal("0.21"));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for SIP scenario")
    void calculateXirr_regularSIPWithFinalReturn_returnsCorrectRate() {
        // Arrange - Simulate monthly SIP of 1000 for a year with final value of 13000
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Month 1
                new BigDecimal("-1000"), // Month 2
                new BigDecimal("-1000"), // Month 3
                new BigDecimal("-1000"), // Month 4
                new BigDecimal("-1000"), // Month 5
                new BigDecimal("-1000"), // Month 6
                new BigDecimal("-1000"), // Month 7
                new BigDecimal("-1000"), // Month 8
                new BigDecimal("-1000"), // Month 9
                new BigDecimal("-1000"), // Month 10
                new BigDecimal("-1000"), // Month 11
                new BigDecimal("-1000"), // Month 12
                new BigDecimal("13000")); // Final value

        LocalDate startDate = LocalDate.of(2020, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(1),
                startDate.plusMonths(2),
                startDate.plusMonths(3),
                startDate.plusMonths(4),
                startDate.plusMonths(5),
                startDate.plusMonths(6),
                startDate.plusMonths(7),
                startDate.plusMonths(8),
                startDate.plusMonths(9),
                startDate.plusMonths(10),
                startDate.plusMonths(11),
                startDate.plusMonths(12));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        // Expected rate around 8.3% for this scenario
        assertThat(result).isCloseTo(new BigDecimal("0.083"), within(new BigDecimal("0.005")));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for negative return scenario")
    void calculateXirr_negativeReturn_returnsCorrectNegativeRate() {
        // Arrange
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Initial investment
                new BigDecimal("900")); // Loss of 10%

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isCloseTo(new BigDecimal("-0.0998"), within(TOLERANCE));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for market crash recovery scenario")
    void calculateXirr_marketCrashAndRecovery_returnsCorrectRate() {
        // Arrange - Simulate investment before market crash, during crash, and recovery
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-10000"), // Initial investment
                new BigDecimal("-5000"), // Buy the dip during crash
                new BigDecimal("-5000"), // Another purchase during low prices
                new BigDecimal("25000")); // Final portfolio value after recovery

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2019, 12, 1), // Pre-crash investment
                LocalDate.of(2020, 3, 15), // During market crash (e.g., COVID-19)
                LocalDate.of(2020, 5, 10), // More investment during recovery
                LocalDate.of(2022, 1, 1)); // Final value after recovery

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - Expecting a positive return despite market volatility
        assertThat(result).isGreaterThanOrEqualTo(new BigDecimal("0.15")); // At least 15% annualized
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for value-averaging investment strategy")
    void calculateXirr_valueAveragingStrategy_returnsCorrectRate() {
        // Arrange - Simulate value-averaging strategy (investing more when prices are down, less when up)
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Initial investment
                new BigDecimal("-1200"), // Market down, invest more
                new BigDecimal("-800"), // Market up, invest less
                new BigDecimal("-1300"), // Market down again
                new BigDecimal("-700"), // Market recovered
                new BigDecimal("5500")); // Final value

        LocalDate startDate = LocalDate.of(2021, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(2),
                startDate.plusMonths(4),
                startDate.plusMonths(6),
                startDate.plusMonths(8),
                startDate.plusMonths(12));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isGreaterThan(new BigDecimal("0.09")); // Expected return > 9%
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for SIP with lump sum addition")
    void calculateXirr_regularSIPWithLumpSum_returnsCorrectRate() {
        // Arrange - Monthly SIP with a lump sum addition in the middle
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Month 1 SIP
                new BigDecimal("-1000"), // Month 2 SIP
                new BigDecimal("-1000"), // Month 3 SIP
                new BigDecimal("-10000"), // Lump sum addition
                new BigDecimal("-1000"), // Month 4 SIP
                new BigDecimal("-1000"), // Month 5 SIP
                new BigDecimal("-1000"), // Month 6 SIP
                new BigDecimal("17000")); // Final value

        LocalDate startDate = LocalDate.of(2022, 1, 10); // SIP dates often aren't month start
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(1),
                startDate.plusMonths(2),
                startDate.plusMonths(2).plusDays(5), // Lump sum a few days after SIP
                startDate.plusMonths(3),
                startDate.plusMonths(4),
                startDate.plusMonths(5),
                startDate.plusMonths(6));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isGreaterThan(new BigDecimal("0.05"));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for partial redemptions")
    void calculateXirr_partialRedemptions_returnsCorrectRate() {
        // Arrange - Initial investment with periodic partial redemptions
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-10000"), // Initial investment
                new BigDecimal("1000"), // Partial redemption 1
                new BigDecimal("1000"), // Partial redemption 2
                new BigDecimal("1000"), // Partial redemption 3
                new BigDecimal("8500")); // Final redemption

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(3),
                startDate.plusMonths(6),
                startDate.plusMonths(9),
                startDate.plusMonths(12));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - Expected return is about 12.89%
        assertThat(result).isCloseTo(new BigDecimal("0.1289"), within(TOLERANCE));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for dividend reinvestment")
    void calculateXirr_dividendReinvestment_returnsCorrectRate() {
        // Arrange - Investment with dividends that are reinvested
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-10000"), // Initial investment
                new BigDecimal("100"), // Dividend
                new BigDecimal("-100"), // Reinvestment of dividend
                new BigDecimal("120"), // Dividend
                new BigDecimal("-120"), // Reinvestment of dividend
                new BigDecimal("150"), // Dividend
                new BigDecimal("-150"), // Reinvestment of dividend
                new BigDecimal("11500")); // Final value

        LocalDate startDate = LocalDate.of(2021, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(3),
                startDate.plusMonths(3), // Same day reinvestment
                startDate.plusMonths(6),
                startDate.plusMonths(6), // Same day reinvestment
                startDate.plusMonths(9),
                startDate.plusMonths(9), // Same day reinvestment
                startDate.plusMonths(12));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isCloseTo(new BigDecimal("0.15"), within(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for multiple goals with different time horizons")
    void calculateXirr_multipleInvestmentGoals_returnsCorrectRate() {
        // Arrange - Multiple investments with different end dates (like retirement + education + house)
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-5000"), // Initial investment
                new BigDecimal("-1000"), // Regular addition
                new BigDecimal("-1000"), // Regular addition
                new BigDecimal("3000"), // First goal redemption (e.g., education)
                new BigDecimal("-1000"), // Continue investing
                new BigDecimal("-1000"), // Continue investing
                new BigDecimal("6000")); // Final redemption (e.g., retirement)

        LocalDate startDate = LocalDate.of(2020, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(6),
                startDate.plusYears(1),
                startDate.plusYears(2), // First goal achieved
                startDate.plusYears(2).plusMonths(6),
                startDate.plusYears(3),
                startDate.plusYears(4)); // Final goal achieved

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isGreaterThan(new BigDecimal("0.08")); // Expected return > 8%
    }

    @Test
    @DisplayName("Should handle XIRR calculation for real SIP with varying amounts")
    void calculateXirr_realSIPWithVaryingAmounts_returnsCorrectRate() {
        // Arrange - Realistic SIP with varying amounts (salary increases, bonuses, etc.)
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-5000"), // Month 1
                new BigDecimal("-5000"), // Month 2
                new BigDecimal("-5000"), // Month 3
                new BigDecimal("-7500"), // Month 4 (increased SIP after salary raise)
                new BigDecimal("-7500"), // Month 5
                new BigDecimal("-15000"), // Month 6 (extra from bonus)
                new BigDecimal("-7500"), // Month 7
                new BigDecimal("-7500"), // Month 8
                new BigDecimal("-7500"), // Month 9
                new BigDecimal("-7500"), // Month 10
                new BigDecimal("-7500"), // Month 11
                new BigDecimal("-7500"), // Month 12
                new BigDecimal("95000")); // Final value

        LocalDate startDate = LocalDate.of(2023, 1, 5); // Realistic SIP date
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(1),
                startDate.plusMonths(2),
                startDate.plusMonths(3),
                startDate.plusMonths(4),
                startDate.plusMonths(5),
                startDate.plusMonths(6),
                startDate.plusMonths(7),
                startDate.plusMonths(8),
                startDate.plusMonths(9),
                startDate.plusMonths(10),
                startDate.plusMonths(11),
                startDate.plusYears(1));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isGreaterThan(new BigDecimal("0.07")); // Expected return > 7%
    }

    @Test
    @DisplayName("Should handle XIRR calculation with very volatile returns")
    void calculateXirr_volatileInvestment_returnsCorrectRate() {
        // Arrange - Simulate very volatile investment returns
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-10000"), // Initial investment
                new BigDecimal("5000"), // Partial redemption during high
                new BigDecimal("-2000"), // Additional investment during dip
                new BigDecimal("7000"), // Partial redemption during recovery
                new BigDecimal("3000")); // Final redemption

        LocalDate startDate = LocalDate.of(2021, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(3),
                startDate.plusMonths(6),
                startDate.plusMonths(9),
                startDate.plusMonths(12));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isGreaterThan(new BigDecimal("0.05")); // Expected return > 5%
    }

    @Test
    @DisplayName("Should throw exception for invalid inputs - null lists")
    void calculateXirr_nullInputs_throwsIllegalArgumentException() {
        // Act & Assert - null cash flows
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> XirrCalculator.calculateXirr(null, Collections.singletonList(LocalDate.now())))
                .withMessageContaining("Invalid cash flows or dates");

        // Act & Assert - null dates
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> XirrCalculator.calculateXirr(Collections.singletonList(BigDecimal.ONE), null))
                .withMessageContaining("Invalid cash flows or dates");
    }

    @Test
    @DisplayName("Should throw exception for invalid inputs - empty lists")
    void calculateXirr_emptyInputs_throwsIllegalArgumentException() {
        // Act & Assert - empty lists
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> XirrCalculator.calculateXirr(Collections.emptyList(), Collections.emptyList()))
                .withMessageContaining("Invalid cash flows or dates");
    }

    @Test
    @DisplayName("Should throw exception for mismatched list sizes")
    void calculateXirr_mismatchedListSizes_throwsIllegalArgumentException() {
        // Arrange
        List<BigDecimal> cashFlows = Arrays.asList(new BigDecimal("-1000"), new BigDecimal("1100"));
        List<LocalDate> dates = Collections.singletonList(LocalDate.now());

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> XirrCalculator.calculateXirr(cashFlows, dates))
                .withMessageContaining("Invalid cash flows or dates");
    }

    @Test
    @DisplayName("Should throw exception if there are no positive cash flows")
    void calculateXirr_noPositiveCashFlows_throwsIllegalArgumentException() {
        // Arrange - only negative cash flows
        List<BigDecimal> cashFlows = Arrays.asList(new BigDecimal("-1000"), new BigDecimal("-500"));

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 7, 1));

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> XirrCalculator.calculateXirr(cashFlows, dates))
                .withMessageContaining("Cash flows must have at least one positive");
    }

    @Test
    @DisplayName("Should throw exception if there are no negative cash flows")
    void calculateXirr_noNegativeCashFlows_throwsIllegalArgumentException() {
        // Arrange - only positive cash flows
        List<BigDecimal> cashFlows = Arrays.asList(new BigDecimal("1000"), new BigDecimal("500"));

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 7, 1));

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> XirrCalculator.calculateXirr(cashFlows, dates))
                .withMessageContaining("Cash flows must have at least one positive and one negative value");
    }

    @Test
    @DisplayName("Should handle very short investment periods")
    void calculateXirr_veryShortInvestmentPeriod_returnsCorrectRate() {
        // Arrange - 10% return in one day
        List<BigDecimal> cashFlows = Arrays.asList(new BigDecimal("-1000"), new BigDecimal("1100"));

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expect a very high annualized return
        assertThat(result).isGreaterThan(new BigDecimal("3600")); // Over 3600% annualized
    }

    @Test
    @DisplayName("Should handle very long investment periods")
    void calculateXirr_veryLongInvestmentPeriod_returnsCorrectRate() {
        // Arrange - Double money in 10 years
        List<BigDecimal> cashFlows = Arrays.asList(new BigDecimal("-1000"), new BigDecimal("2000"));

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2010, 1, 1), LocalDate.of(2020, 1, 1));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expect around 7.18% annual return
        assertThat(result).isCloseTo(new BigDecimal("0.0718"), within(new BigDecimal("0.0005")));
    }

    @Test
    @DisplayName("Should handle small decimal cash flows")
    void calculateXirr_smallDecimalCashFlows_returnsCorrectRate() {
        // Arrange - Small decimal values
        List<BigDecimal> cashFlows = Arrays.asList(new BigDecimal("-0.001"), new BigDecimal("0.0011"));

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - should be 10% return regardless of scale
        assertThat(result).isCloseTo(new BigDecimal("0.09978"), within(TOLERANCE));
    }
}
