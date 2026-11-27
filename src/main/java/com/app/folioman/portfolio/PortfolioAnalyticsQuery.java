package com.app.folioman.portfolio;

import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioResponse;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioAnalyticsQuery {

    boolean isPanOwnedByEmail(String pan, String email);

    Optional<InvestmentReturnsDTO> getInvestmentReturnsByPan(String pan, String email);

    List<MonthlyInvestmentResponseDTO> getTotalInvestmentsByPanPerMonth(String pan, String email);

    List<YearlyInvestmentResponseDTO> getTotalInvestmentsByPanPerYear(String pan, String email);

    PortfolioResponse getPortfolioByPAN(String panNumber, String email, LocalDate asOfDate);
}
