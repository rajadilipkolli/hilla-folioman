package com.app.folioman.portfolio.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class UserTransactionsControllerIT extends AbstractIntegrationTest {

    @Test
    void getTotalInvestmentsByPanPerMonth() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", "ABCDE1234F").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].month").exists())
                .andExpect(jsonPath("$[*].totalInvestment").exists());
    }

    @Test
    void getTotalInvestmentsByPanPerMonth_WithInvalidPan_ShouldReturnBadRequest() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", "INVALID-PAN").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTotalInvestmentsByPanPerMonth_WithNonExistentPan_ShouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", "ZZZZZ9999Z").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
