package com.example.application.portfolio.service;

import com.example.application.portfolio.repository.InvestorInfoRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class InvestorInfoService {

    private final InvestorInfoRepository investorInfoRepository;

    public InvestorInfoService(InvestorInfoRepository investorInfoRepository) {
        this.investorInfoRepository = investorInfoRepository;
    }

    @Cacheable(value = "emailAndName", unless = "#result == false")
    public boolean existsByEmailAndName(String email, String name) {
        return this.investorInfoRepository.existsByEmailAndName(email, name);
    }
}
