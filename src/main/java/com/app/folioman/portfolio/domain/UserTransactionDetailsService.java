package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import com.app.folioman.portfolio.util.XirrCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserTransactionDetailsService {

    private final UserTransactionDetailsRepository userTransactionDetailsRepository;
    private final UserPortfolioValueRepository userPortfolioValueRepository;

    UserTransactionDetailsService(
            UserTransactionDetailsRepository userTransactionDetailsRepository,
            UserPortfolioValueRepository userPortfolioValueRepository) {
        this.userTransactionDetailsRepository = userTransactionDetailsRepository;
        this.userPortfolioValueRepository = userPortfolioValueRepository;
    }

    /**
     * Retrieves the latest investment returns (XIRR and CAGR) for a given PAN.
     *
     * @param pan The PAN to query
     * @return An Optional containing the InvestmentReturnsDTO, or empty if no data exists
     */
    Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan) {
        return userPortfolioValueRepository.getLatestPortfolioValueByPan(pan).map(projection -> {
            BigDecimal cagr = null;
            Optional<LocalDate> firstTransactionDate =
                    userTransactionDetailsRepository.findMinTransactionDateByPan(pan);

            if (firstTransactionDate.isPresent()) {
                long days = ChronoUnit.DAYS.between(firstTransactionDate.get(), projection.getDate());
                if (days > 0) {
                    cagr = XirrCalculator.cagr(projection.getInvested(), projection.getValue(), days);
                }
            }

            return new InvestmentReturnsDTO(
                    projection.getXirr(), cagr, projection.getInvested(), projection.getValue(), projection.getDate());
        });
    }

    Long findAllTransactionsByEmailNameAndPeriod(String name, String email, LocalDate from, LocalDate to) {
        return userTransactionDetailsRepository.findAllTransactionByEmailAndNameAndInRange(email, name, from, to);
    }

    List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan) {
        return userTransactionDetailsRepository.findMonthlyInvestmentsByPan(pan).stream()
                .map(MonthlyInvestmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan) {
        return userTransactionDetailsRepository.findYearlyInvestmentsByPan(pan).stream()
                .map(YearlyInvestmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    List<UserTransactionDetailsEntity> saveTransactions(List<UserTransactionDetailsEntity> transactions) {
        return userTransactionDetailsRepository.saveAll(transactions);
    }
}
