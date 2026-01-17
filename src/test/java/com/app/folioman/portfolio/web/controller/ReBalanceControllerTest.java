package com.app.folioman.portfolio.web.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.models.request.Fund;
import com.app.folioman.portfolio.models.request.InvestmentRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ReBalanceController.class)
class ReBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void rebalanceCalculation() throws Exception {
        // Create test data
        List<Fund> funds = List.of(new Fund(5000.0, 0.4), new Fund(3000.0, 0.3), new Fund(2000.0, 0.3));

        InvestmentRequest request = new InvestmentRequest(funds, 1000.0);

        // Expected calculations:
        // Total current value: 5000 + 3000 + 2000 = 10000
        // Total with new investment: 10000 + 1000 = 11000
        // Expected investments:
        // Fund A: Target = 0.4 * 11000 = 4400, Investment = 4400 - 5000 = -600
        // Fund B: Target = 0.3 * 11000 = 3300, Investment = 3300 - 3000 = 300
        // Fund C: Target = 0.3 * 11000 = 3300, Investment = 3300 - 2000 = 1300

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investments[0]").value(-600.0))
                .andExpect(jsonPath("$.investments[1]").value(300.0))
                .andExpect(jsonPath("$.investments[2]").value(1300.0));
    }

    @Test
    void rebalanceWithEmptyFundList() throws Exception {
        // Create test data with empty fund list
        InvestmentRequest request = new InvestmentRequest(List.of(), 1000.0);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Changed from isOk() to isBadRequest()
    }

    @Test
    void rebalanceWithLargeNumbers() throws Exception {
        // Create test data with larger numbers
        List<Fund> funds = List.of(new Fund(500000.0, 0.5), new Fund(500000.0, 0.5));

        InvestmentRequest request = new InvestmentRequest(funds, 100000.0);

        // Total: 1000000 + 100000 = 1100000
        // Fund A: Target = 0.5 * 1100000 = 550000, Investment = 550000 - 500000 = 50000
        // Fund B: Target = 0.5 * 1100000 = 550000, Investment = 550000 - 500000 = 50000

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investments[0]").value(50000.0))
                .andExpect(jsonPath("$.investments[1]").value(50000.0));
    }

    @Test
    void reBalance() throws Exception {
        this.mockMvc
                .perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new InvestmentRequest(
                                List.of(new Fund(7000.00, 0.7), new Fund(3000.00, 0.25), new Fund(500.00, 0.05)),
                                1000.00)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.investments.size()", is(3)));
    }

    @Test
    void reBalanceWithNullInvestmentRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio/rebalance").content("{}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/rebalance")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("amountToInvest")))
                .andExpect(jsonPath("$.violations[0].message", is("Amount to invest cannot be null")))
                .andExpect(jsonPath("$.violations[1].field", is("funds")))
                .andExpect(jsonPath(
                        "$.violations[1].message", is("Investment request and funds list cannot be null or empty")));
    }

    @Test
    void reBalanceWithEmptyFundsList() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(), // Empty funds list
                1000.00);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/rebalance")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("funds")))
                .andExpect(jsonPath(
                        "$.violations[0].message", is("Investment request and funds list cannot be null or empty")));
    }

    @Test
    void reBalanceWithNegativeAmountToInvest() throws Exception {
        InvestmentRequest investmentRequest =
                new InvestmentRequest(List.of(new Fund(10000.00, 0.5)), -1000.00); // Negative amount to invest

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/rebalance")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("amountToInvest")))
                .andExpect(jsonPath("$.violations[0].message", is("Amount to invest cannot be negative")))
                .andExpect(jsonPath("$.violations[1].field", is("funds")))
                .andExpect(jsonPath(
                        "$.violations[1].message", is("The sum of fund ratios must be 1%. Current sum: 0.50")));
    }

    @Test
    void reBalanceWithInvalidFundRatios() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(
                        new Fund(10000.0, 0.50), new Fund(10000.0, 0.30)
                        // Sum of ratios is 0.80, not 1.00
                        ),
                1000.00);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/rebalance")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("funds")))
                .andExpect(jsonPath(
                        "$.violations[0].message", is("The sum of fund ratios must be 1%. Current sum: 0.80")));
    }

    @Test
    void reBalanceWithNegativeFundValue() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(new Fund(-10000.00, 0.5)), // Negative fund value
                1000.00);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/rebalance")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("funds")))
                .andExpect(
                        jsonPath("$.violations[0].message", is("The sum of fund ratios must be 1%. Current sum: 0.50")))
                .andExpect(jsonPath("$.violations[1].field", is("funds[0].value")))
                .andExpect(jsonPath("$.violations[1].message", is("Fund value cannot be negative")));
    }

    @Test
    void reBalanceWithInvalidFundRatio() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(new Fund(10000.00, 1.50)), // Ratio greater than 100
                1000.00);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/rebalance")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("funds")))
                .andExpect(
                        jsonPath("$.violations[0].message", is("The sum of fund ratios must be 1%. Current sum: 1.50")))
                .andExpect(jsonPath("$.violations[1].field", is("funds[0].ratio")))
                .andExpect(jsonPath("$.violations[1].message", is("Fund ratio must be between 0 and 1")));
    }
}
