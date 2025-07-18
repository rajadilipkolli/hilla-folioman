package com.app.folioman.portfolio.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.models.request.Fund;
import com.app.folioman.portfolio.models.request.InvestmentRequest;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ReBalanceControllerIT extends AbstractIntegrationTest {

    @Test
    void testRebalanceCalculation() throws Exception {
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
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investments[0]").value(-600.0))
                .andExpect(jsonPath("$.investments[1]").value(300.0))
                .andExpect(jsonPath("$.investments[2]").value(1300.0));
    }

    @Test
    void testRebalanceWithEmptyFundList() throws Exception {
        // Create test data with empty fund list
        InvestmentRequest request = new InvestmentRequest(List.of(), 1000.0);

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Changed from isOk() to isBadRequest()
    }

    @Test
    void testRebalanceWithLargeNumbers() throws Exception {
        // Create test data with larger numbers
        List<Fund> funds = List.of(new Fund(500000.0, 0.5), new Fund(500000.0, 0.5));

        InvestmentRequest request = new InvestmentRequest(funds, 100000.0);

        // Total: 1000000 + 100000 = 1100000
        // Fund A: Target = 0.5 * 1100000 = 550000, Investment = 550000 - 500000 = 50000
        // Fund B: Target = 0.5 * 1100000 = 550000, Investment = 550000 - 500000 = 50000

        mockMvc.perform(post("/api/portfolio/rebalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investments[0]").value(50000.0))
                .andExpect(jsonPath("$.investments[1]").value(50000.0));
    }
}
