package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.entities.UserTransactionDetails;
import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponseDTO;
import com.app.folioman.portfolio.repository.UserTransactionDetailsRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserTransactionDetailsService {

    private final UserTransactionDetailsRepository userTransactionDetailsRepository;

    UserTransactionDetailsService(UserTransactionDetailsRepository userTransactionDetailsRepository) {
        this.userTransactionDetailsRepository = userTransactionDetailsRepository;
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
