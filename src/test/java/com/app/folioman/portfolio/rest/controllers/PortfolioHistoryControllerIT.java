package com.app.folioman.portfolio.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.domain.CasTypeEnum;
import com.app.folioman.portfolio.domain.FileTypeEnum;
import com.app.folioman.portfolio.domain.InvestorInfoEntity;
import com.app.folioman.portfolio.domain.UserCasDetailsEntity;
import com.app.folioman.portfolio.domain.UserPortfolioValueEntity;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class PortfolioHistoryControllerIT extends AbstractIntegrationTest {

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
        Long casId = insertTestPortfolioHistory("user", LocalDate.now().minusDays(20));
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
                .andExpect(jsonPath("$.invested").isNotEmpty())
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value").isNotEmpty());
    }

    @Test
    @DisplayName("Should default to the last year when from/to are omitted")
    void shouldUseDefaultDateRangeWhenParametersAreOmitted() throws Exception {
        Long casId = insertTestPortfolioHistory("user", LocalDate.now().minusDays(200));

        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/history", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invested").isArray())
                .andExpect(jsonPath("$.invested").isNotEmpty())
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value").isNotEmpty());
    }

    private Long insertTestPortfolioHistory(String email, LocalDate date) {
        return transactionTemplate.execute(status -> {
            UserCasDetailsEntity casDetails = new UserCasDetailsEntity()
                    .setCasTypeEnum(CasTypeEnum.DETAILED)
                    .setFileTypeEnum(FileTypeEnum.CAMS);

            InvestorInfoEntity investorInfo = new InvestorInfoEntity()
                    .setEmail(email)
                    .setName("Integration Test User")
                    .setMobile("9999999999")
                    .setAddress("Test Address")
                    .setUserCasDetailsEntity(casDetails);

            casDetails.setInvestorInfoEntity(investorInfo);

            entityManager.persist(casDetails);
            entityManager.flush();
            Long casId = casDetails.getId();

            UserPortfolioValueEntity valueEntity = new UserPortfolioValueEntity()
                    .setDate(date)
                    .setInvested(BigDecimal.valueOf(1000L))
                    .setValue(BigDecimal.valueOf(1100L))
                    .setXirr(BigDecimal.valueOf(10.5))
                    .setLiveXirr(BigDecimal.valueOf(12.5))
                    .setUserCasDetails(casDetails);

            entityManager.persist(valueEntity);
            entityManager.flush();

            return casId;
        });
    }
}
