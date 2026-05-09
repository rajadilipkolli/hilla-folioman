package com.app.folioman.portfolio.rest.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.portfolio.domain.PortfolioAPI;
import com.app.folioman.portfolio.rest.dtos.InvestmentReturnsDTO;
import com.app.folioman.portfolio.rest.dtos.MonthlyInvestmentResponseDTO;
import com.app.folioman.portfolio.rest.dtos.YearlyInvestmentResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
    private PortfolioAPI portfolioAPI;

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

    @Test
    void getInvestmentReturns_HappyPath() throws Exception {
        String pan = "ABCDE1234F";
        InvestmentReturnsDTO returnsDTO = new InvestmentReturnsDTO(
                new BigDecimal("15.5"),
                new BigDecimal("12.3"),
                new BigDecimal("10000"),
                new BigDecimal("15000"),
                LocalDate.now());

        when(portfolioAPI.getInvestmentReturnsByPan(pan)).thenReturn(Optional.of(returnsDTO));

        this.mockMvc
                .perform(get("/api/portfolio/returns/{pan}", pan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.xirr", is(15.5)))
                .andExpect(jsonPath("$.invested", is(10000)));

        verify(portfolioAPI).getInvestmentReturnsByPan(pan);
    }

    @Test
    void getTotalInvestmentsByPanPerMonth_HappyPath() throws Exception {
        String pan = "ABCDE1234F";
        List<MonthlyInvestmentResponseDTO> response =
                List.of(new MonthlyInvestmentResponseDTO(2023, 1, new BigDecimal("1000"), new BigDecimal("1000")));

        when(portfolioAPI.getTotalInvestmentsByPanPerMonth(pan)).thenReturn(response);

        this.mockMvc
                .perform(get("/api/portfolio/investments/{pan}", pan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].year", is(2023)))
                .andExpect(jsonPath("$[0].investmentPerMonth", is(1000)));

        verify(portfolioAPI).getTotalInvestmentsByPanPerMonth(pan);
    }

    @Test
    void getTotalInvestmentsByPanPerYear_HappyPath() throws Exception {
        String pan = "ABCDE1234F";
        List<YearlyInvestmentResponseDTO> response =
                List.of(new YearlyInvestmentResponseDTO(2023, new BigDecimal("12000")));

        when(portfolioAPI.getTotalInvestmentsByPanPerYear(pan)).thenReturn(response);

        this.mockMvc
                .perform(get("/api/portfolio/investments/yearly/{pan}", pan).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].year", is(2023)))
                .andExpect(jsonPath("$[0].yearlyInvestment", is(12000)));

        verify(portfolioAPI).getTotalInvestmentsByPanPerYear(pan);
    }
}
