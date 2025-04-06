package com.app.folioman.portfolio.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

    @Test
    @DisplayName("Should handle extreme values with large investments and returns")
    void calculateXirr_extremeValues_returnsCorrectRate() {
        // Arrange - Very large values
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-10000000"), // 10 million initial investment
                new BigDecimal("15000000")); // 15 million return

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2023, 1, 1)); // 3-year period

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expect around 14.47% annual return
        assertThat(result).isCloseTo(new BigDecimal("0.1447"), within(TOLERANCE));
    }

    @Test
    @DisplayName("Should handle zero return scenario (break-even)")
    void calculateXirr_breakEven_returnsZero() {
        // Arrange - Break-even investment (0% return)
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-1000"), // Initial investment
                new BigDecimal("1000")); // Exactly same amount returned

        List<LocalDate> dates = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expect 0% return
        assertThat(result).isCloseTo(BigDecimal.ZERO, within(TOLERANCE));
    }

    @Test
    @DisplayName("Should handle irregular SIP dates properly")
    void calculateXirr_irregularDates_returnsCorrectRate() {
        // Arrange - Realistic irregular investment pattern (not fixed dates)
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-5000"), // Initial investment
                new BigDecimal("-3000"), // Random addition
                new BigDecimal("-4500"), // Another addition
                new BigDecimal("-2800"), // Another addition
                new BigDecimal("19000")); // Final value

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2022, 1, 15), // Initial date
                LocalDate.of(2022, 3, 7), // Not exactly one month later
                LocalDate.of(2022, 5, 22), // Irregular date
                LocalDate.of(2022, 8, 14), // Irregular date
                LocalDate.of(2023, 1, 10)); // Slightly earlier than one year

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expect a positive return
        assertThat(result).isGreaterThan(new BigDecimal("0.12")); // > 12% return
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for retirement withdrawal plan")
    void calculateXirr_retirementWithdrawals_returnsCorrectRate() {
        // Arrange - Initial lump sum followed by regular withdrawals (retirement scenario)
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-500000"), // Retirement corpus
                new BigDecimal("20000"), // Monthly withdrawal
                new BigDecimal("20000"), // Monthly withdrawal
                new BigDecimal("20000"), // Monthly withdrawal
                new BigDecimal("20000"), // Monthly withdrawal
                new BigDecimal("20000"), // Monthly withdrawal
                new BigDecimal("400000")); // Remaining corpus after 5 months

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        List<LocalDate> dates = Arrays.asList(
                startDate,
                startDate.plusMonths(1),
                startDate.plusMonths(2),
                startDate.plusMonths(3),
                startDate.plusMonths(4),
                startDate.plusMonths(5),
                startDate.plusMonths(5)); // Remaining corpus on same date as last withdrawal

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - expect a reasonable withdrawal rate
        assertThat(result).isCloseTo(new BigDecimal("0.036"), within(new BigDecimal("0.005"))); // Around 3.6%
    }

    @Test
    @DisplayName("Should calculate XIRR correctly for mixed investment types (MF, stocks, etc)")
    void calculateXirr_mixedInvestments_returnsCorrectRate() {
        // Arrange - Simulate different investments with different timing and returns
        List<BigDecimal> cashFlows = Arrays.asList(
                new BigDecimal("-5000"), // MF investment
                new BigDecimal("-10000"), // Stock purchase
                new BigDecimal("2500"), // Stock dividend
                new BigDecimal("-3000"), // More MF investment
                new BigDecimal("11000"), // Stock sale
                new BigDecimal("7000")); // MF redemption

        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2022, 1, 15),
                LocalDate.of(2022, 2, 10),
                LocalDate.of(2022, 5, 30),
                LocalDate.of(2022, 7, 12),
                LocalDate.of(2022, 9, 5),
                LocalDate.of(2023, 1, 10));

        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isGreaterThan(new BigDecimal("0.10")); // Expect > 10% return
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("basicInvestmentScenarios")
    @DisplayName("Should calculate XIRR correctly for various basic scenarios")
    void calculateXirr_basicScenarios_returnsCorrectRate(
            String scenarioName, List<BigDecimal> cashFlows, List<LocalDate> dates, BigDecimal expectedRate) {
        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isCloseTo(expectedRate, within(TOLERANCE));
    }

    private static Stream<Arguments> basicInvestmentScenarios() {
        return Stream.of(
                // Single year 10% return
                Arguments.of(
                        "Single year 10% return",
                        Arrays.asList(new BigDecimal("-1000"), new BigDecimal("1100")),
                        Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1)),
                        new BigDecimal("0.09978")),
                // Two-year 20% total return (9.54% annually)
                Arguments.of(
                        "Two-year 20% total return",
                        Arrays.asList(new BigDecimal("-1000"), new BigDecimal("1200")),
                        Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2022, 1, 1)),
                        new BigDecimal("0.0954")),
                // Single year negative 10% return
                Arguments.of(
                        "Single year negative 10% return",
                        Arrays.asList(new BigDecimal("-1000"), new BigDecimal("900")),
                        Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1)),
                        new BigDecimal("-0.0998")),
                // Break-even (0% return)
                Arguments.of(
                        "Break-even (0% return)",
                        Arrays.asList(new BigDecimal("-1000"), new BigDecimal("1000")),
                        Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1)),
                        BigDecimal.ZERO),
                // Half-year 10% return (21.55% annualized)
                Arguments.of(
                        "Half-year 10% return",
                        Arrays.asList(new BigDecimal("-1000"), new BigDecimal("1100")),
                        Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 7, 1)),
                        new BigDecimal("0.2155")),
                // Three-year doubling of money (26% annually)
                Arguments.of(
                        "Three-year doubling of money",
                        Arrays.asList(new BigDecimal("-1000"), new BigDecimal("2000")),
                        Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2023, 1, 1)),
                        new BigDecimal("0.26")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("multipleFlowScenarios")
    @DisplayName("Should calculate XIRR correctly for multiple cash flow scenarios")
    void calculateXirr_multipleFlowScenarios_returnsCorrectRate(
            String scenarioName,
            List<BigDecimal> cashFlows,
            List<LocalDate> dates,
            BigDecimal expectedRate,
            BigDecimal tolerance) {
        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert
        assertThat(result).isCloseTo(expectedRate, within(tolerance));
    }

    private static Stream<Arguments> multipleFlowScenarios() {
        return Stream.of(
                // Multiple flows with intermediate redemption
                Arguments.of(
                        "Multiple flows with intermediate redemption",
                        Arrays.asList(
                                new BigDecimal("-1000"), // Initial investment
                                new BigDecimal("-500"), // Additional investment
                                new BigDecimal("750"), // Partial withdrawal
                                new BigDecimal("1000") // Final return
                                ),
                        Arrays.asList(
                                LocalDate.of(2020, 1, 1),
                                LocalDate.of(2020, 4, 1),
                                LocalDate.of(2020, 10, 1),
                                LocalDate.of(2021, 1, 1)),
                        new BigDecimal("0.20"), // ~20% return
                        new BigDecimal("0.01") // 1% tolerance
                        ),
                // Partial redemptions pattern
                Arguments.of(
                        "Partial redemptions pattern",
                        Arrays.asList(
                                new BigDecimal("-10000"), // Initial investment
                                new BigDecimal("1000"), // Partial redemption 1
                                new BigDecimal("1000"), // Partial redemption 2
                                new BigDecimal("1000"), // Partial redemption 3
                                new BigDecimal("8500") // Final redemption
                                ),
                        Arrays.asList(
                                LocalDate.of(2022, 1, 1),
                                LocalDate.of(2022, 4, 1),
                                LocalDate.of(2022, 7, 1),
                                LocalDate.of(2022, 10, 1),
                                LocalDate.of(2023, 1, 1)),
                        new BigDecimal("0.1289"), // 12.89% return
                        TOLERANCE),
                // Market crash and recovery
                Arguments.of(
                        "Market crash and recovery",
                        Arrays.asList(
                                new BigDecimal("-10000"), // Initial investment
                                new BigDecimal("-5000"), // Buy the dip
                                new BigDecimal("-5000"), // More buying
                                new BigDecimal("25000") // Final value after recovery
                                ),
                        Arrays.asList(
                                LocalDate.of(2019, 12, 1),
                                LocalDate.of(2020, 3, 15),
                                LocalDate.of(2020, 5, 10),
                                LocalDate.of(2022, 1, 1)),
                        new BigDecimal("0.15"), // ~15% return
                        new BigDecimal("0.01") // 1% tolerance
                        ),
                // Withdrawal strategy (retirement scenario)
                Arguments.of(
                        "Withdrawal strategy (retirement)",
                        Arrays.asList(
                                new BigDecimal("-500000"), // Initial corpus
                                new BigDecimal("20000"), // Monthly withdrawal
                                new BigDecimal("20000"), // Monthly withdrawal
                                new BigDecimal("20000"), // Monthly withdrawal
                                new BigDecimal("420000") // Remaining corpus
                                ),
                        Arrays.asList(
                                LocalDate.of(2023, 1, 1),
                                LocalDate.of(2023, 2, 1),
                                LocalDate.of(2023, 3, 1),
                                LocalDate.of(2023, 4, 1),
                                LocalDate.of(2023, 4, 1) // Same date as last withdrawal
                                ),
                        new BigDecimal("0.036"), // ~3.6% return
                        new BigDecimal("0.005") // 0.5% tolerance
                        ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sipScenarios")
    @DisplayName("Should calculate XIRR correctly for various SIP scenarios")
    void calculateXirr_sipScenarios_returnsExpectedRate(
            String scenarioName, List<BigDecimal> cashFlows, List<LocalDate> dates, BigDecimal expectedMinimumRate) {
        // Act
        BigDecimal result = XirrCalculator.calculateXirr(cashFlows, dates);

        // Assert - for SIPs we just check the minimum expected rate
        assertThat(result).isGreaterThanOrEqualTo(expectedMinimumRate);
    }

    private static Stream<Arguments> sipScenarios() {
        return Stream.of(
                // Regular monthly SIP
                Arguments.of(
                        "Regular monthly SIP with positive return",
                        generateMonthlySipCashFlows(12, new BigDecimal("1000"), new BigDecimal("13000")),
                        generateMonthlySipDates(LocalDate.of(2022, 1, 1), 12),
                        new BigDecimal("0.08") // At least 8% return
                        ),
                // Regular quarterly SIP
                Arguments.of(
                        "Quarterly SIP with positive return",
                        Arrays.asList(
                                new BigDecimal("-1000"), // Q1
                                new BigDecimal("-1000"), // Q2
                                new BigDecimal("-1000"), // Q3
                                new BigDecimal("-1000"), // Q4
                                new BigDecimal("4500") // Final value
                                ),
                        Arrays.asList(
                                LocalDate.of(2022, 1, 15),
                                LocalDate.of(2022, 4, 15),
                                LocalDate.of(2022, 7, 15),
                                LocalDate.of(2022, 10, 15),
                                LocalDate.of(2023, 1, 15)),
                        new BigDecimal("0.10") // At least 10% return
                        ),
                // SIP with varying amounts
                Arguments.of(
                        "SIP with varying amounts",
                        Arrays.asList(
                                new BigDecimal("-5000"), // Month 1
                                new BigDecimal("-5000"), // Month 2
                                new BigDecimal("-7500"), // Month 3 (increased)
                                new BigDecimal("-7500"), // Month 4
                                new BigDecimal("-10000"), // Month 5 (increased)
                                new BigDecimal("-5000"), // Month 6 (decreased)
                                new BigDecimal("45000") // Final value
                                ),
                        Arrays.asList(
                                LocalDate.of(2022, 1, 5),
                                LocalDate.of(2022, 2, 5),
                                LocalDate.of(2022, 3, 5),
                                LocalDate.of(2022, 4, 5),
                                LocalDate.of(2022, 5, 5),
                                LocalDate.of(2022, 6, 5),
                                LocalDate.of(2022, 7, 5)),
                        new BigDecimal("0.07") // At least 7% return
                        ),
                // SIP with lump sum
                Arguments.of(
                        "SIP with mid-term lump sum addition",
                        Arrays.asList(
                                new BigDecimal("-1000"), // Month 1
                                new BigDecimal("-1000"), // Month 2
                                new BigDecimal("-10000"), // Lump sum
                                new BigDecimal("-1000"), // Month 3
                                new BigDecimal("-1000"), // Month 4
                                new BigDecimal("15000") // Final value
                                ),
                        Arrays.asList(
                                LocalDate.of(2022, 1, 10),
                                LocalDate.of(2022, 2, 10),
                                LocalDate.of(2022, 2, 15), // Lump sum a few days later
                                LocalDate.of(2022, 3, 10),
                                LocalDate.of(2022, 4, 10),
                                LocalDate.of(2022, 7, 10) // 6 months total
                                ),
                        new BigDecimal("0.05") // At least 5% return
                        ));
    }

    // Helper method to generate monthly SIP cash flows
    private static List<BigDecimal> generateMonthlySipCashFlows(
            int months, BigDecimal monthlyAmount, BigDecimal finalValue) {
        BigDecimal[] flows = new BigDecimal[months + 1];
        for (int i = 0; i < months; i++) {
            flows[i] = monthlyAmount.negate(); // Investments are negative cash flows
        }
        flows[months] = finalValue; // Final redemption value
        return Arrays.asList(flows);
    }

    // Helper method to generate monthly SIP dates
    private static List<LocalDate> generateMonthlySipDates(LocalDate startDate, int months) {
        LocalDate[] dates = new LocalDate[months + 1];
        for (int i = 0; i <= months; i++) {
            dates[i] = startDate.plusMonths(i);
        }
        return Arrays.asList(dates);
    }
}
