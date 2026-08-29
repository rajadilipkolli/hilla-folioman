package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.app.folioman.portfolio.rest.dtos.PortfolioHistoryDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioHistoryServiceTest {

    @Mock
    private UserTransactionDetailsService userTransactionDetailsService;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private PdfProcessingService pdfProcessingService;

    @Mock
    private UserCASDetailsRepository userCASDetailsRepository;

    @Mock
    private UserPortfolioValueRepository userPortfolioValueRepository;

    @InjectMocks
    private PortfolioAPIImpl portfolioAPI;

    @Test
    void shouldReturnEmptyWhenCasDoesNotExist() {
        when(userCASDetailsRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<PortfolioHistoryDTO> result = portfolioAPI.getPortfolioHistory(
                1L, "user@example.com", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenUserEmailDoesNotMatchOwner() {
        UserCasDetailsEntity casDetails = new UserCasDetailsEntity();
        InvestorInfoEntity investorInfo = new InvestorInfoEntity();
        investorInfo.setEmail("owner@example.com");
        casDetails.setInvestorInfoEntity(investorInfo);

        when(userCASDetailsRepository.findById(1L)).thenReturn(Optional.of(casDetails));

        Optional<PortfolioHistoryDTO> result = portfolioAPI.getPortfolioHistory(
                1L, "otheruser@example.com", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTransformPortfolioValuesIntoOrderedHistoryDto() {
        UserCasDetailsEntity casDetails = new UserCasDetailsEntity();
        InvestorInfoEntity investorInfo = new InvestorInfoEntity();
        investorInfo.setEmail("user@example.com");
        casDetails.setInvestorInfoEntity(investorInfo);

        UserPortfolioValueEntity later = new UserPortfolioValueEntity()
                .setDate(LocalDate.of(2024, 11, 1))
                .setInvested(BigDecimal.valueOf(1200))
                .setValue(BigDecimal.valueOf(2100));
        UserPortfolioValueEntity earlier = new UserPortfolioValueEntity()
                .setDate(LocalDate.of(2024, 10, 1))
                .setInvested(BigDecimal.valueOf(1000))
                .setValue(BigDecimal.valueOf(2000));

        when(userCASDetailsRepository.findById(1L)).thenReturn(Optional.of(casDetails));
        when(userPortfolioValueRepository.findByUserCasDetailsEntity_IdAndDateBetween(
                        eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(later, earlier));

        Optional<PortfolioHistoryDTO> result = portfolioAPI.getPortfolioHistory(
                1L, "user@example.com", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

        assertThat(result).isPresent();

        PortfolioHistoryDTO historyDTO = result.get();
        assertThat(historyDTO.invested()).hasSize(2);
        assertThat(historyDTO.value()).hasSize(2);

        assertThat(historyDTO.invested().get(0)[0])
                .isEqualTo(LocalDate.of(2024, 10, 1)
                        .atStartOfDay(java.time.ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli());
        assertThat(historyDTO.invested().get(0)[1]).isEqualTo(BigDecimal.valueOf(1000L));
        assertThat(historyDTO.invested().get(1)[0])
                .isEqualTo(LocalDate.of(2024, 11, 1)
                        .atStartOfDay(java.time.ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli());
        assertThat(historyDTO.invested().get(1)[1]).isEqualTo(BigDecimal.valueOf(1200L));

        assertThat(historyDTO.value().get(0)[1]).isEqualTo(BigDecimal.valueOf(2000L));
        assertThat(historyDTO.value().get(1)[1]).isEqualTo(BigDecimal.valueOf(2100L));
    }
}
