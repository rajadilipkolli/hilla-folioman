package com.app.folioman.portfolio;

import com.app.folioman.portfolio.rest.dtos.PortfolioHistoryDTO;
import com.app.folioman.portfolio.rest.dtos.PortfolioSummaryDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioSummaryQuery {

    List<PortfolioSummaryProjection> getPortfolioSummariesByEmail(String email);

    Optional<PortfolioHistoryDTO> getPortfolioHistory(Long casId, String userEmail, LocalDate from, LocalDate to);

    Optional<PortfolioSummaryDTO> getPortfolioSummary(Long casId, String userEmail);
}
