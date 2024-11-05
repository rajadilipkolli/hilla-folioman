package com.app.folioman.mfschemes.web.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.app.folioman.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class NavControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldThrowExceptionWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 159999).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("NAV Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", containsString("Nav Not Found for schemeCode - 159999 on")))
                .andExpect(jsonPath("$.instance", is("/api/nav/159999")));
    }

    @Test
    void shouldLoadDataWhenSchemeFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}", 120503L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("AXIS ELSS TAX SAVER FUND - DIRECT GROWTH")))
                .andExpect(jsonPath("$.nav", notNullValue(String.class)))
                .andExpect(jsonPath("$.date", notNullValue(String.class)));
    }

    @Test
    void shouldLoadDataWhenSchemeFoundAndLoadHistoricalData() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 120503L, "2022-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(120503L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF846K01EW2")))
                .andExpect(jsonPath("$.schemeName", is("AXIS ELSS TAX SAVER FUND - DIRECT GROWTH")))
                .andExpect(jsonPath("$.nav", is("73.60850")))
                .andExpect(jsonPath("$.date", is("2022-12-20")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended(Equity Scheme - ELSS)")));
    }

    @Test
    void shouldLoadDataWhenSchemeNotFoundAndLoadHistoricalData() throws Exception {

        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 119578L, "2018-12-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(119578L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF903J01MV8")))
                .andExpect(jsonPath("$.schemeName", is("Sundaram Select Focus Direct Plan - Growth")))
                .andExpect(jsonPath("$.nav", is("176.60910")))
                .andExpect(jsonPath("$.amc", is("Sundaram Asset Management Company Ltd")))
                .andExpect(jsonPath("$.date", is("2018-12-20")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended(Equity Scheme - Focused Fund)")));
    }

    @Test
    void shouldNotLoadHistoricalDataWhenSchemeNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 144610L, "2023-07-12")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("NAV Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Nav Not Found for schemeCode - 144610 on 2023-07-06")))
                .andExpect(jsonPath("$.instance", is("/api/nav/144610/2023-07-12")));
    }

    @Test
    void shouldNotLoadDataWhenSchemeFoundAndLoadHistoricalDataNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 141565, "2017-10-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("NAV Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Nav Not Found for schemeCode - 141565 on 2017-09-25")))
                .andExpect(jsonPath("$.instance", is("/api/nav/141565/2017-10-01")));
    }

    @Test
    void shouldLoadDataWhenSchemeMergedWithOtherFundHouse() throws Exception {
        this.mockMvc
                .perform(get("/api/nav/{schemeCode}/{date}", 151113, "2022-10-20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.schemeCode", is(151113L), Long.class))
                .andExpect(jsonPath("$.isin", is("INF917K01HD4")))
                .andExpect(jsonPath("$.schemeName", is("HSBC VALUE FUND DIRECT PLAN - GROWTH")))
                .andExpect(jsonPath("$.nav", is("63.16200")))
                .andExpect(jsonPath("$.date", is("2022-10-20")))
                .andExpect(jsonPath("$.schemeType", is("Open Ended(Equity Scheme - Value Fund)")));
    }
}
