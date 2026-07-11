package com.app.folioman.portfolio.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;

class PortfolioSummaryControllerIT extends AbstractIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("Should return unauthorized when no authentication token is provided")
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/1/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return not found when portfolio is not owned by current user")
    void shouldReturnNotFoundForNonOwnedPortfolio() throws Exception {
        Long casId = insertTestPortfolioSummaryData("otheruser@example.com");

        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/summary", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return portfolio summary for owned portfolio and populate cache")
    void shouldReturnSummaryForOwnedPortfolioAndPopulateCache() throws Exception {
        String email = "user";
        Long casId = insertTestPortfolioSummaryData(email);

        // 1. Verify cache is empty initially

        this.mockMvc
                .perform(get("/api/mutualfunds/portfolio/{id}/summary", casId)
                        .with(testUser())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.invested").value(1000.0))
                .andExpect(jsonPath("$.value").value(1100.0))
                .andExpect(jsonPath("$.xirr.current").value(12.5))
                .andExpect(jsonPath("$.xirr.overall").value(10.5)) // mapped to XirrDTO
                .andExpect(jsonPath("$.change.D").isNumber())
                .andExpect(jsonPath("$.change.A").isNumber())
                .andExpect(jsonPath("$.change_pct.D").isNumber())
                .andExpect(jsonPath("$.change_pct.A").isNumber())
                .andExpect(jsonPath("$.date").exists())
                .andExpect(jsonPath("$.schemes").isArray())
                .andExpect(jsonPath("$.schemes[0].nav0").isNumber())
                .andExpect(jsonPath("$.schemes[0].nav1").isNumber())
                .andExpect(jsonPath("$.schemes[0].folios").isArray());

        Set<String> cacheKeys = redisTemplate.keys(CacheNames.SUMMARY_CACHE + "::*");
        assertThat(cacheKeys).isNotNull();
        assertThat(cacheKeys.stream().anyMatch(key -> key.contains("summary_" + casId + "_")))
                .isTrue();
    }

    private Long insertTestPortfolioSummaryData(String email) {
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
                    .setDate(LocalDate.now())
                    .setInvested(BigDecimal.valueOf(1000L))
                    .setValue(BigDecimal.valueOf(1100L))
                    .setXirr(BigDecimal.valueOf(10.5))
                    .setLiveXirr(BigDecimal.valueOf(12.5))
                    .setUserCasDetails(casDetails);
            entityManager.persist(valueEntity);

            UserFolioDetailsEntity folio = new UserFolioDetailsEntity()
                    .setFolio("12345678")
                    .setAmc("Test AMC")
                    .setPan("ABCDE1234F")
                    .setUserCasDetailsEntity(casDetails);
            entityManager.persist(folio);

            UserSchemeDetailsEntity scheme = new UserSchemeDetailsEntity()
                    .setScheme("Test Scheme")
                    .setIsin("INF123456789")
                    .setAmfi(120503L)
                    .setUserFolioDetails(folio);
            entityManager.persist(scheme);

            SchemeValueEntity schemeValue = new SchemeValueEntity()
                    .setDate(LocalDate.now())
                    .setInvested(BigDecimal.valueOf(1000L))
                    .setValue(BigDecimal.valueOf(1100L))
                    .setBalance(BigDecimal.valueOf(10.0))
                    .setAvgNav(BigDecimal.valueOf(100.0))
                    .setUserSchemeDetails(scheme);
            entityManager.persist(schemeValue);

            FolioSchemeEntity folioScheme = new FolioSchemeEntity()
                    .setXirr(BigDecimal.valueOf(15.0))
                    .setUserFolioDetailsEntity(folio)
                    .setUserSchemeDetailsEntity(scheme);
            entityManager.persist(folioScheme);

            entityManager.flush();
            return casId;
        });
    }
}
