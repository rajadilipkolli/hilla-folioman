package com.app.folioman.mfschemes.rest.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(roles = "USER")
class NavControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldThrowExceptionWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 159999).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/nav-not-found")))
                .andExpect(jsonPath("$.title", is("NAV Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", containsString("Nav Not Found for schemeCode - 159999 on")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath());
    }

    @Test
    void shouldLoadDataWhenSchemeFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 120503L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis ELSS Tax Saver Fund - Direct Plan - Growth Option")))
                .andExpect(jsonPath("$.nav", notNullValue(String.class)))
                .andExpect(jsonPath("$.date", notNullValue(String.class)));
    }

    @Test
    void shouldReturnStoredHistoricalNavAsDecimal() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 120503L, "2022-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("Axis ELSS Tax Saver Fund - Direct Plan - Growth Option")))
                .andExpect(jsonPath("$.nav", is("73.60850")))
                .andExpect(jsonPath("$.date", is("2022-12-20")))
                .andExpect(
                        jsonPath("$.schemeType", is("Open Ended Schemes ( Equity Schemes - ELSS- Tax Saver Fund )")));
    }

    @Test
    void shouldFallBackWhenHistoricalNavIsNotStored() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 119578L, "2018-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(119578L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF903J01MV8")))
                .andExpect(jsonPath("$.schemeName", is("Sundaram Select Focus Direct Plan - Growth")))
                .andExpect(jsonPath("$.nav", is("176.6091")))
                .andExpect(jsonPath("$.amc", is("Sundaram Mutual Fund")))
                .andExpect(jsonPath("$.date", is("2018-12-20")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended Schemes ( Equity Scheme - Focused Fund )")));
    }

    @Test
    void shouldReturnFallbackNavOnOrBeforeRequestedDate() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 144610L, "2023-07-12")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemeCode", is(144610L), Long.class))
                .andExpect(jsonPath("$.nav", is("12.7142")))
                .andExpect(jsonPath("$.date", is("2021-08-26")));
    }

    @Test
    void shouldReportMissingHistoricalNavForRequestedDate() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 141565, "2017-10-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/nav-not-found")))
                .andExpect(jsonPath("$.title", is("NAV Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Nav Not Found for schemeCode - 141565 on 2017-10-01")))
                .andExpect(jsonPath("$.instance").doesNotHaveJsonPath());
    }

    @Test
    void shouldReportMissingNavForRetiredSchemeCode() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 151113, "2022-10-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.hilla-folioman.com/errors/nav-not-found")))
                .andExpect(jsonPath("$.detail", is("Nav Not Found for schemeCode - 151113 on 2022-10-20")));
    }
}
