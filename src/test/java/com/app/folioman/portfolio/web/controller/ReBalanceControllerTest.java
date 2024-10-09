package com.app.folioman.portfolio.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                        .content(objectMapper.writeValueAsString(new InvestmentRequest(
                                List.of(new Fund(7000, 70), new Fund(3000, 25), new Fund(500, 5)), 1000)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
