package com.app.folioman.portfolio.web.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.service.UserTransactionDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserTransactionsController.class)
class UserTransactionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserTransactionDetailsService userTransactionDetailsService;

    @Test
    void getTotalInvestmentsByPanPerMonth() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", "ABCD1234EF").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath(
                        "$.detail", matchesPattern("getTotalInvestmentsByPanPerMonth.pan: Invalid PAN number format")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/investments/ABCD1234EF")));
    }

    @Test
    void getTotalInvestmentsByPanPerYear() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/yearly/{pan}", "ABCD1234EF")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/validation-error")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath(
                        "$.detail", matchesPattern("getTotalInvestmentsByPanPerYear.pan: Invalid PAN number format")))
                .andExpect(jsonPath("$.instance", is("/api/portfolio/investments/yearly/ABCD1234EF")));
    }
}
