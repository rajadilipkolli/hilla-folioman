package com.app.folioman.portfolio.service;

import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponse;
import com.app.folioman.portfolio.repository.UserTransactionDetailsRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
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

    public List<MonthlyInvestmentResponse> getTotalInvestmentsByPanPerMonth(String pan) {
        return userTransactionDetailsRepository.findMonthlyInvestmentsByPan(pan);
    }
}
