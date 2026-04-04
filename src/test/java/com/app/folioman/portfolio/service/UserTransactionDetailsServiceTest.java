package com.app.folioman.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.app.folioman.portfolio.models.projection.UserPortfolioValueProjection;
import com.app.folioman.portfolio.models.response.InvestmentReturnsDTO;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.app.folioman.portfolio.repository.UserTransactionDetailsRepository;
import com.app.folioman.portfolio.util.XirrCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTransactionDetailsServiceTest {

    @Mock
    private UserTransactionDetailsRepository userTransactionDetailsRepository;

    @Mock
    private UserPortfolioValueRepository userPortfolioValueRepository;

    @InjectMocks
    private UserTransactionDetailsService userTransactionDetailsService;

    @Test
    void testGetInvestmentReturnsByPan() {
        String pan = "ABCDE1234F";
        LocalDate valuationDate = LocalDate.of(2024, 1, 1);
        LocalDate firstDate = LocalDate.of(2023, 1, 1);
        BigDecimal invested = new BigDecimal("1000");
        BigDecimal value = new BigDecimal("1100");
        BigDecimal xirr = new BigDecimal("10.0");

        UserPortfolioValueProjection projection = new UserPortfolioValueProjection() {
            @Override
            public BigDecimal getXirr() {
                return xirr;
            }

            @Override
            public BigDecimal getLiveXirr() {
                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal getInvested() {
                return invested;
            }

            @Override
            public BigDecimal getValue() {
                return value;
            }

            @Override
            public LocalDate getDate() {
                return valuationDate;
            }
        };

        given(userPortfolioValueRepository.getLatestPortfolioValueByPan(pan)).willReturn(Optional.of(projection));
        given(userTransactionDetailsRepository.findMinTransactionDateByPan(pan)).willReturn(Optional.of(firstDate));

        Optional<InvestmentReturnsDTO> result = userTransactionDetailsService.getInvestmentReturnsByPan(pan);

        assertThat(result).isPresent();
        assertThat(result.get().xirr()).isEqualTo(xirr);
        assertThat(result.get().invested()).isEqualTo(invested);
        assertThat(result.get().currentValue()).isEqualTo(value);
        assertThat(result.get().valuationDate()).isEqualTo(valuationDate);
        // Verify exact CAGR value based on XirrCalculator.cagr(1000, 1100, 365)
        BigDecimal expectedCagr = XirrCalculator.cagr(invested, value, 365);
        assertThat(result.get().cagr()).isEqualByComparingTo(expectedCagr);
    }

    @Test
    void testGetInvestmentReturnsByPanNotFound() {
        String pan = "NONEXISTENT";
        given(userPortfolioValueRepository.getLatestPortfolioValueByPan(pan)).willReturn(Optional.empty());

        Optional<InvestmentReturnsDTO> result = userTransactionDetailsService.getInvestmentReturnsByPan(pan);

        assertThat(result).isEmpty();
    }

    @Test
    void testGetInvestmentReturnsByPan_WhenNoTransactionDate_CagrIsNull() {
        String pan = "ABCDE1234F";
        UserPortfolioValueProjection projection = new UserPortfolioValueProjection() {
            @Override
            public BigDecimal getXirr() {
                return new BigDecimal("10.0");
            }

            @Override
            public BigDecimal getLiveXirr() {
                return BigDecimal.ZERO;
            }

            @Override
            public BigDecimal getInvested() {
                return new BigDecimal("1000");
            }

            @Override
            public BigDecimal getValue() {
                return new BigDecimal("1100");
            }

            @Override
            public LocalDate getDate() {
                return LocalDate.of(2024, 1, 1);
            }
        };

        given(userPortfolioValueRepository.getLatestPortfolioValueByPan(pan)).willReturn(Optional.of(projection));
        given(userTransactionDetailsRepository.findMinTransactionDateByPan(pan)).willReturn(Optional.empty());

        Optional<InvestmentReturnsDTO> result = userTransactionDetailsService.getInvestmentReturnsByPan(pan);

        assertThat(result).isPresent();
        assertThat(result.get().cagr()).isNull();
    }
}
