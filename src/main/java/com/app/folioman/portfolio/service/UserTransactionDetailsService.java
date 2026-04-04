package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.response.InvestmentReturnsDTO;
import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponseDTO;
import com.app.folioman.portfolio.repository.UserPortfolioValueRepository;
import com.app.folioman.portfolio.repository.UserTransactionDetailsRepository;
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
    public Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan) {
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

    public Long findAllTransactionsByEmailNameAndPeriod(String name, String email, LocalDate from, LocalDate to) {
        return userTransactionDetailsRepository.findAllTransactionByEmailAndNameAndInRange(email, name, from, to);
    }

    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan) {
        return userTransactionDetailsRepository.findMonthlyInvestmentsByPan(pan).stream()
                .map(MonthlyInvestmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan) {
        return userTransactionDetailsRepository.findYearlyInvestmentsByPan(pan).stream()
                .map(YearlyInvestmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<UserTransactionDetails> saveTransactions(List<UserTransactionDetails> transactions) {
        return userTransactionDetailsRepository.saveAll(transactions);
    }
}
