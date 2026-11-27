package com.app.folioman.portfolio.domain;

import com.app.folioman.shared.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class InvestorInfoService {

    private final InvestorInfoRepository investorInfoRepository;

    InvestorInfoService(InvestorInfoRepository investorInfoRepository) {
        this.investorInfoRepository = investorInfoRepository;
    }

    @Cacheable(value = CacheNames.EMAIL_AND_NAME_CACHE, unless = "#result == false")
    public boolean existsByEmailAndName(String email, String name) {
        return this.investorInfoRepository.existsByEmailAndName(email, name);
    }
}
