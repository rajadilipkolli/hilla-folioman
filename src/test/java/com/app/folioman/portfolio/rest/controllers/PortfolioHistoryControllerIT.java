package com.app.folioman.portfolio.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

class PortfolioHistoryControllerIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should return unauthorized when no authentication token is provided")
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/1/history").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return not found when portfolio is not owned by current user")
    void shouldReturnNotFoundForNonOwnedPortfolio() throws Exception {
        Long casId = insertTestPortfolioHistory(
                "otheruser@example.com", LocalDate.now().minusDays(30));

        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/history", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return portfolio history for owned portfolio and populate cache")
    void shouldReturnHistoryForOwnedPortfolioAndPopulateCache() throws Exception {
        Long casId = insertTestPortfolioHistory("user", LocalDate.now().minusDays(30));

        MvcResult firstResult = this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/history", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.invested").isArray())
                .andExpect(jsonPath("$.value").isArray())
                .andReturn();

        Set<String> cacheKeys = redisTemplate.keys(CacheNames.PORTFOLIO_HISTORY_CACHE + "::*");
        assertThat(cacheKeys).isNotNull();
        assertThat(cacheKeys.stream().anyMatch(key -> key.contains("history_" + casId + "_")))
                .isTrue();

        MvcResult secondResult = this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/history", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(secondResult.getResponse().getContentAsString())
                .isEqualTo(firstResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Should apply explicit from/to query parameters when provided")
    void shouldApplyExplicitFromToQueryParameters() throws Exception {
        Long casId = insertTestPortfolioHistory("user", LocalDate.now().minusDays(100));
        LocalDate fromDate = LocalDate.now().minusDays(40);
        LocalDate toDate = LocalDate.now();

        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/history", casId)
                        .with(testUser())
                        .param("from", fromDate.toString())
                        .param("to", toDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invested").isArray())
                .andExpect(jsonPath("$.value").isArray());
    }

    @Test
    @DisplayName("Should default to the last year when from/to are omitted")
    void shouldUseDefaultDateRangeWhenParametersAreOmitted() throws Exception {
        Long casId = insertTestPortfolioHistory("user", LocalDate.now().minusDays(400));

        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/history", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invested").isArray())
                .andExpect(jsonPath("$.value").isArray());
    }

    private Long insertTestPortfolioHistory(String email, LocalDate date) {
        Long casId = jdbcTemplate.queryForObject("select nextval('portfolio.user_cas_details_seq')", Long.class);
        jdbcTemplate.update(
                "insert into portfolio.user_cas_details (id, cas_type, file_type, created_at, updated_at) values (?, ?, ?, now(), now())",
                casId,
                "CAMS",
                "DETAILED");
        jdbcTemplate.update(
                "insert into portfolio.investor_info (id, email, name, mobile, address, user_cas_details_id, created_at, updated_at) values (?, ?, ?, ?, ?, ?, now(), now())",
                casId,
                email,
                "Integration Test User",
                "9999999999",
                "Test Address",
                casId);

        Long valueId = jdbcTemplate.queryForObject("select nextval('portfolio.user_portfolio_value_seq')", Long.class);
        jdbcTemplate.update(
                "insert into portfolio.user_portfolio_value (id, date, invested, value, xirr, live_xirr, user_cas_details_id, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, now(), now())",
                valueId,
                Date.valueOf(date),
                BigDecimal.valueOf(1000L),
                BigDecimal.valueOf(1200L),
                null,
                null,
                casId);

        return casId;
    }
}
