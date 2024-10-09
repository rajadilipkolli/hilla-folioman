package com.app.folioman.portfolio.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.models.request.Fund;
import com.app.folioman.portfolio.models.request.InvestmentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ReBalanceController.class)
@AutoConfigureMockMvc
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
                                List.of(new Fund(7000, 70), new Fund(3000, 25), new Fund(500, 5)), 1000)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.investments.size()", is(3)));
    }

    @Test
    void testReBalanceWithNullInvestmentRequest() throws Exception {
        mockMvc.perform(post("/api/portfolio/rebalance").content("{}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().string(containsString("Investment request and funds list cannot be null or empty")));
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
                .andExpect(
                        content().string(containsString("Investment request and funds list cannot be null or empty")));
    }

    @Test
    void testReBalanceWithNegativeAmountToInvest() throws Exception {
        InvestmentRequest investmentRequest =
                new InvestmentRequest(List.of(new Fund(10000, 0.5)), -1000); // Negative amount to invest

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Amount to invest cannot be negative")));
    }

    @Test
    void testReBalanceWithInvalidFundRatios() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(
                        new Fund(10000, 50), new Fund(10000, 30)
                        // Sum of ratios is 80, not 100
                        ),
                1000);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Sum of fund ratios must equal 1")));
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
                .andExpect(content().string(containsString("Fund value cannot be negative")));
    }

    @Test
    void testReBalanceWithInvalidFundRatio() throws Exception {
        InvestmentRequest investmentRequest = new InvestmentRequest(
                List.of(new Fund(10000, 150)), // Ratio greater than 100
                1000);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Fund ratio must be between 0 and 1")));
    }
}
