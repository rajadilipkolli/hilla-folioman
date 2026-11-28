package com.app.folioman.portfolio.rest.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.auth.domain.JwtService;
import com.app.folioman.auth.domain.TokenBlacklistService;
import com.app.folioman.portfolio.ReBalanceUseCase;
import com.app.folioman.portfolio.rest.dtos.Fund;
import com.app.folioman.portfolio.rest.dtos.InvestmentRequest;
import com.app.folioman.portfolio.rest.dtos.InvestmentResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = ReBalanceController.class)
@WithMockUser(roles = "USER")
class ReBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ReBalanceUseCase reBalanceUseCase;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void rebalanceCalculation() throws Exception {
        List<Fund> funds = List.of(new Fund(5000.0, 0.4), new Fund(3000.0, 0.3), new Fund(2000.0, 0.3));
        InvestmentRequest request = new InvestmentRequest(funds, 1000.0);
        when(reBalanceUseCase.reBalance(any(InvestmentRequest.class)))
                .thenReturn(new InvestmentResponse(List.of(-600.0, 300.0, 1300.0)));

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investments[0]").value(-600.0))
                .andExpect(jsonPath("$.investments[1]").value(300.0))
                .andExpect(jsonPath("$.investments[2]").value(1300.0));
    }

    @Test
    void rebalanceWithLargeNumbers() throws Exception {
        List<Fund> funds = List.of(new Fund(500000.0, 0.5), new Fund(500000.0, 0.5));
        InvestmentRequest request = new InvestmentRequest(funds, 100000.0);
        when(reBalanceUseCase.reBalance(any(InvestmentRequest.class)))
                .thenReturn(new InvestmentResponse(List.of(50000.0, 50000.0)));

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investments[0]").value(50000.0))
                .andExpect(jsonPath("$.investments[1]").value(50000.0));
    }

    @Test
    void reBalance() throws Exception {
        when(reBalanceUseCase.reBalance(any(InvestmentRequest.class)))
                .thenReturn(new InvestmentResponse(List.of(1050.0, -125.0, 75.0)));

        this.mockMvc
                .perform(post("/api/portfolio/rebalance")
                        .with(csrf())
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
        mockMvc.perform(post("/api/portfolio/rebalance")
                        .with(csrf())
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath())
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath())
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath())
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath())
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath())
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
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(investmentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("funds")))
                .andExpect(
                        jsonPath("$.violations[0].message", is("The sum of fund ratios must be 1%. Current sum: 1.50")))
                .andExpect(jsonPath("$.violations[1].field", is("funds[0].ratio")))
                .andExpect(jsonPath("$.violations[1].message", is("Fund ratio must be between 0 and 1")));
    }
}
