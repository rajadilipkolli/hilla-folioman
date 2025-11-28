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
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ReBalanceController.class)
class ReBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testReBalance() throws Exception {
        this.mockMvc
                .perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InvestmentRequest(
                                List.of(new Fund(7000, 0.7), new Fund(3000, 0.25), new Fund(500, 0.05)), 1000)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.investments.size()", is(3)));
    }

    @Test
    void testReBalanceWithNullInvestmentRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio/rebalance").content("{}").contentType(MediaType.APPLICATION_JSON))
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
    void testReBalanceWithEmptyFundsList() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(), // Empty funds list
                1000);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
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
    void testReBalanceWithNegativeAmountToInvest() throws Exception {
        InvestmentRequest investmentRequest =
                new InvestmentRequest(List.of(new Fund(10000, 0.5)), -1000); // Negative amount to invest

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
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
    void testReBalanceWithInvalidFundRatios() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(
                        new Fund(10000, 0.50), new Fund(10000, 0.30)
                        // Sum of ratios is 0.80, not 1.00
                        ),
                1000);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
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
    void testReBalanceWithNegativeFundValue() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(new Fund(-10000, 0.5)), // Negative fund value
                1000);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
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
    void testReBalanceWithInvalidFundRatio() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(new Fund(10000, 1.50)), // Ratio greater than 100
                1000);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
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
