package com.example.application.portfolio.service;

import com.example.application.portfolio.repository.UserTransactionDetailsRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserTransactionDetailsService {

    private final UserTransactionDetailsRepository userTransactionDetailsRepository;

    UserTransactionDetailsService(UserTransactionDetailsRepository userTransactionDetailsRepository) {
        this.userTransactionDetailsRepository = userTransactionDetailsRepository;
    }

    public Long findAllTransactionsByEmailNameAndPeriod(String name, String email, LocalDate from, LocalDate to) {
        return userTransactionDetailsRepository.findAllTransactionByEmailAndNameAndInRange(email, name, from, to);
    }
}
