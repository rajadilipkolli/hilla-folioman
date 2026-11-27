package com.app.folioman.portfolio.domain;

import com.app.folioman.portfolio.PortfolioAnalyticsQuery;
import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import com.app.folioman.shared.CacheNames;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class PortfolioAnalyticsQueryImpl implements PortfolioAnalyticsQuery {

    private final UserTransactionDetailsService userTransactionDetailsService;
    private final UserDetailService userDetailService;
    private final UserFolioDetailsRepository userFolioDetailsRepository;

    PortfolioAnalyticsQueryImpl(
            UserTransactionDetailsService userTransactionDetailsService,
            UserDetailService userDetailService,
            UserFolioDetailsRepository userFolioDetailsRepository) {
        this.userTransactionDetailsService = userTransactionDetailsService;
        this.userDetailService = userDetailService;
        this.userFolioDetailsRepository = userFolioDetailsRepository;
    }

    @Override
    public boolean isPanOwnedByEmail(String pan, String email) {
        return userFolioDetailsRepository.existsByPanAndEmailIgnoreCase(pan, email);
    }

    @Override
    @Cacheable(
            cacheNames = CacheNames.RETURNS_CACHE,
            key = "'returns_' + #pan + '_' + #email",
            unless = "#result == null")
    public Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan, String email) {
        return userTransactionDetailsService.getInvestmentReturnsByPan(pan, email);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.TRANSACTION_CACHE, key = "'monthly_' + #pan + '_' + #email")
    public List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan, String email) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerMonth(pan, email);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.TRANSACTION_CACHE, key = "'yearly_' + #pan + '_' + #email")
    public List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan, String email) {
        return userTransactionDetailsService.getTotalInvestmentsByPanPerYear(pan, email);
    }

    @Override
    public PortfolioResponse getPortfolioByPAN(String panNumber, String email, LocalDate asOfDate) {
        return userDetailService.getPortfolioByPAN(panNumber, email, asOfDate);
    }
}
