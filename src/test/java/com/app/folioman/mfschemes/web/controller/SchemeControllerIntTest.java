package com.app.folioman.mfschemes.web.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.folioman.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class SchemeControllerIntTest extends AbstractIntegrationTest {

    @Test
    void fetchSchemes_byFundName() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "sbi small cap").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.size()", is(4)))
                .andExpect(jsonPath("$[*].amfiCode", contains(125494, 125495, 125496, 125497)))
                .andExpect(jsonPath("$[*].amcName", everyItem(equalTo("SBI Funds Management Limited"))));
    }

    @Test
    void fetchSchemes_byAmcName() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "amc sbi").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$[0].amcName", equalTo("SBI Funds Management Limited")))
                .andExpect(jsonPath("$[*].amcName", everyItem(equalTo("SBI Funds Management Limited"))));
    }

    @Test
    void fetchSchemes_byAmcFullName() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "SBI Funds Management Limited")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$[*].amcName", everyItem(equalTo("SBI Funds Management Limited"))));
    }

    @Test
    void fetchSchemes_bySchemeCode() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "125494").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amfiCode", is(125494)))
                .andExpect(jsonPath("$[0].amcName", equalTo("SBI Funds Management Limited")));
    }

    @Test
    void testFetchSchemesWithNoResults() throws Exception {
        // Test with a query that should not match any schemes
        String nonExistentSchemeName = "ThisSchemeDoesNotExistAnywhere12345";

        mockMvc.perform(get("/api/scheme/{schemeName}", nonExistentSchemeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testFetchSchemesWithPartialMatch() throws Exception {
        // Test with partial name that should match multiple schemes
        mockMvc.perform(get("/api/scheme/{schemeName}", "equity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }
}
