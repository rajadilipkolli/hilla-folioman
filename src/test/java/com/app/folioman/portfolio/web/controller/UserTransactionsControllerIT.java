package com.app.folioman.portfolio.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.config.redis.CacheNames;
import com.app.folioman.portfolio.models.response.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.models.response.YearlyInvestmentResponseDTO;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTransactionsControllerIT extends AbstractIntegrationTest {

    private static final String TEST_PAN = "ABCDE1234F";

    @Test
    @Order(1)
    void getTotalInvestmentsByPanPerMonth() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(2)
    void getTotalInvestmentsByPanPerMonth_WithInvalidPan_ShouldReturnBadRequest() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", "INVALID-PAN").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void getTotalInvestmentsByPanPerYear() throws Exception {
        this.mockMvc
                .perform(
                        get("/api/portfolio/investments/yearly/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    void getTotalInvestmentsByPanPerYear_WithInvalidPan_ShouldReturnBadRequest() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/yearly/{pan}", "INVALID-PAN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("Should cache transaction data and use cache on subsequent requests")
    void shouldCacheTransactionDataAndUseCache() throws Exception {
        // 1. Clear any existing cache to ensure clean test state
        clearCacheForPan(TEST_PAN);

        // 2. First request should hit the database and populate the cache
        MvcResult firstMonthlyResult = this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult firstYearlyResult = this.mockMvc
                .perform(
                        get("/api/portfolio/investments/yearly/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 3. Verify cache keys were created
        Set<String> cacheKeys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean monthlyKeyExists = cacheKeys.stream().anyMatch(key -> key.contains("monthly_" + TEST_PAN));
        boolean yearlyKeyExists = cacheKeys.stream().anyMatch(key -> key.contains("yearly_" + TEST_PAN));

        assertThat(monthlyKeyExists).isTrue();
        assertThat(yearlyKeyExists).isTrue();

        // 4. Second request should use cached data
        MvcResult secondMonthlyResult = this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondYearlyResult = this.mockMvc
                .perform(
                        get("/api/portfolio/investments/yearly/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 5. Verify responses are identical (content served from cache)
        String firstMonthlyResponse = firstMonthlyResult.getResponse().getContentAsString();
        String secondMonthlyResponse = secondMonthlyResult.getResponse().getContentAsString();
        String firstYearlyResponse = firstYearlyResult.getResponse().getContentAsString();
        String secondYearlyResponse = secondYearlyResult.getResponse().getContentAsString();

        assertThat(secondMonthlyResponse).isEqualTo(firstMonthlyResponse);
        assertThat(secondYearlyResponse).isEqualTo(firstYearlyResponse);
    }

    @Test
    @Order(6)
    @DisplayName("Should evict cache and refresh data when eviction is triggered")
    void shouldEvictCacheAndRefreshData() throws Exception {
        // 1. Verify we have existing cache entries before eviction
        Set<String> cacheKeysBefore = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean hasMonthlyKey = cacheKeysBefore.stream().anyMatch(key -> key.contains("monthly_" + TEST_PAN));
        boolean hasYearlyKey = cacheKeysBefore.stream().anyMatch(key -> key.contains("yearly_" + TEST_PAN));

        // Confirm cache exists from previous test
        assertThat(hasMonthlyKey || hasYearlyKey).isTrue();

        // 2. Get data before eviction
        MvcResult beforeEvictionMonthly = this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<MonthlyInvestmentResponseDTO> monthlyBefore = objectMapper.readValue(
                beforeEvictionMonthly.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, MonthlyInvestmentResponseDTO.class));

        // 3. Evict the cache
        evictTransactionCaches();

        // 4. Verify cache was cleared
        Set<String> cacheKeysAfter = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean monthlyKeyGone = cacheKeysAfter.stream().noneMatch(key -> key.contains("monthly_" + TEST_PAN));
        boolean yearlyKeyGone = cacheKeysAfter.stream().noneMatch(key -> key.contains("yearly_" + TEST_PAN));

        assertThat(monthlyKeyGone).isTrue();
        assertThat(yearlyKeyGone).isTrue();

        // 5. Call endpoint again - should repopulate cache
        MvcResult afterEvictionMonthly = this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", TEST_PAN).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<MonthlyInvestmentResponseDTO> monthlyAfter = objectMapper.readValue(
                afterEvictionMonthly.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, MonthlyInvestmentResponseDTO.class));

        // 6. Verify cache was repopulated
        Set<String> cacheKeysNew = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean newMonthlyKeyExists = cacheKeysNew.stream().anyMatch(key -> key.contains("monthly_" + TEST_PAN));

        assertThat(newMonthlyKeyExists).isTrue();

        // 7. Verify data was correctly loaded again (ensure same size, even if it's empty)
        assertThat(monthlyAfter.size()).isEqualTo(monthlyBefore.size());
    }

    @Test
    @Order(7)
    @DisplayName("Should have separate caches for different users")
    void shouldHaveSeparateCachesForDifferentUsers() throws Exception {
        // Use a different PAN to verify separate caching
        final String otherPan = "XYZAB1234C";

        // Clear any existing cache for this PAN
        clearCacheForPan(otherPan);

        // Fetch data for second PAN
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", otherPan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify both cache entries exist
        Set<String> cacheKeys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean originalPanCached = cacheKeys.stream().anyMatch(key -> key.contains(TEST_PAN));
        boolean newPanCached = cacheKeys.stream().anyMatch(key -> key.contains(otherPan));

        assertThat(originalPanCached).isTrue();
        assertThat(newPanCached).isTrue();
    }

    @Test
    @Order(8)
    @DisplayName("Should use cached data after uploading mutual fund data")
    void shouldUseCachedDataAfterUploadingMutualFundData() throws Exception {
        final String testPan = "ZZAAA1234Z";

        // Clear cache for this specific PAN
        clearCacheForPan(testPan);

        // 1. Create test CAS data with the specific PAN
        File tempFile = File.createTempFile("test-cas", ".json");
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(
                    """
                    {
                    	"statement_period": {
                    		"from": "01-Jan-2020",
                    		"to": "20-Jun-2024"
                    	},
                    	"file_type": "CAMS",
                    	"cas_type": "DETAILED",
                    	"investor_info": {
                    		"email": "cache.test@example.com",
                    		"name": "Cache Test User",
                    		"mobile": "9999999999",
                    		"address": "Test Address"
                    	},
                    	"folios": [
                    		{
                    			"folio": "99999999 / 99",
                    			"amc": "HDFC Mutual Fund",
                    			"schemes": [
                    				{
                    					"scheme": "HDFC Index Fund-NIFTY 50 Plan - Direct Growth (Non-Demat) - ISIN: INF234K01VS4",
                    					"isin": "INF234K01VS4",
                    					"amfi": 125745,
                    					"advisor": "INA000000000",
                    					"type": "EQUITY",
                    					"rta": "CAMS",
                    					"close": "500.000",
                    					"rta_code": "HDFC50",
                    					"open": "0.0",
                    					"close_calculated": "500.000",
                    					"valuation": {
                    						"date": "2024-04-16",
                    						"nav": 200.00,
                    						"value": 100000.0
                    					},
                    					"transactions": [
                    						{
                    							"date": "2023-01-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 100.000,
                    							"nav": 100.0,
                    							"balance": 100.000,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2023-02-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 95.238,
                    							"nav": 105.0,
                    							"balance": 195.238,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2023-03-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 90.909,
                    							"nav": 110.0,
                    							"balance": 286.147,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2024-01-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 52.632,
                    							"nav": 190.0,
                    							"balance": 338.779,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2024-02-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 51.282,
                    							"nav": 195.0,
                    							"balance": 390.061,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2024-03-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 50.000,
                    							"nav": 200.0,
                    							"balance": 440.061,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						},
                    						{
                    							"date": "2024-04-10",
                    							"description": "SIP Purchase",
                    							"amount": 10000.0,
                    							"units": 59.939,
                    							"nav": 200.0,
                    							"balance": 500.000,
                    							"type": "PURCHASE_SIP",
                    							"dividend_rate": null
                    						}
                    					]
                    				}
                    			],
                    			"PAN": "ZZAAA1234Z",
                    			"KYC": "OK",
                    			"PANKYC": "OK"
                    		}
                    	]
                    }
                    """);
        }

        try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {
            // 2. Upload the mutual fund data
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", "test-cas-file.json", MediaType.APPLICATION_JSON_VALUE, fileInputStream);

            mockMvc.perform(multipart("/api/upload-handler").file(multipartFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.newFolios", is(1)))
                    .andExpect(jsonPath("$.newSchemes", is(1)))
                    .andExpect(jsonPath("$.newTransactions", is(7)));
        } finally {
            tempFile.deleteOnExit();
        }

        // 3. Wait for processing to complete
        await().atMost(Duration.ofSeconds(5)).pollDelay(Duration.ofMillis(500)).until(() -> {
            Set<String> keys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
            return !keys.isEmpty();
        });

        // 4. First call to get monthly investments - should hit the database
        MvcResult firstMonthlyResult = mockMvc.perform(
                        get("/api/portfolio/investments/{pan}", testPan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 5. First call to get yearly investments - should hit the database
        MvcResult firstYearlyResult = mockMvc.perform(
                        get("/api/portfolio/investments/yearly/{pan}", testPan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 6. Verify cache entries were created
        Set<String> cacheKeys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        boolean monthlyKeyCached = cacheKeys.stream().anyMatch(key -> key.contains("monthly_" + testPan));
        boolean yearlyKeyCached = cacheKeys.stream().anyMatch(key -> key.contains("yearly_" + testPan));

        assertThat(monthlyKeyCached).isTrue();
        assertThat(yearlyKeyCached).isTrue();

        // 7. Second calls - should use cached data
        MvcResult secondMonthlyResult = mockMvc.perform(
                        get("/api/portfolio/investments/{pan}", testPan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondYearlyResult = mockMvc.perform(
                        get("/api/portfolio/investments/yearly/{pan}", testPan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 8. Verify responses are identical (content served from cache)
        assertThat(secondMonthlyResult.getResponse().getContentAsString())
                .isEqualTo(firstMonthlyResult.getResponse().getContentAsString());
        assertThat(secondYearlyResult.getResponse().getContentAsString())
                .isEqualTo(firstYearlyResult.getResponse().getContentAsString());

        // 9. Verify data matches the imported transactions
        List<YearlyInvestmentResponseDTO> yearlyData = objectMapper.readValue(
                secondYearlyResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, YearlyInvestmentResponseDTO.class));

        assertThat(yearlyData).hasSize(2); // Should have data for 2023 and 2024

        // Find 2023 data - should have 3 transactions of 10000 each
        YearlyInvestmentResponseDTO data2023 =
                yearlyData.stream().filter(y -> y.year() == 2023).findFirst().orElseThrow();

        // Find 2024 data - should have 4 transactions of 10000 each
        YearlyInvestmentResponseDTO data2024 =
                yearlyData.stream().filter(y -> y.year() == 2024).findFirst().orElseThrow();

        assertThat(data2023.yearlyInvestment()).isEqualByComparingTo(new BigDecimal("30000.00"));
        assertThat(data2024.yearlyInvestment()).isEqualByComparingTo(new BigDecimal("40000.00"));
    }

    /**
     * Helper method to evict transaction caches - performs same operation as the
     * PortfolioCacheConfig.evictTransactionCaches() method but directly in the test
     */
    private void evictTransactionCaches() {
        String cacheKeyPattern = CacheNames.TRANSACTION_CACHE + "::*";
        Set<String> keys = redisTemplate.keys(cacheKeyPattern);

        if (!keys.isEmpty()) {
            for (String key : keys) {
                if (key.contains("monthly_") || key.contains("yearly_")) {
                    redisTemplate.delete(key);
                }
            }
        }
    }

    /**
     * Helper method to clear cache for a specific PAN
     */
    private void clearCacheForPan(String pan) {
        Set<String> keys = redisTemplate.keys(CacheNames.TRANSACTION_CACHE + "::*");
        if (!keys.isEmpty()) {
            keys.stream().filter(key -> key.contains("_" + pan)).forEach(key -> redisTemplate.delete(key));
        }
    }
}
