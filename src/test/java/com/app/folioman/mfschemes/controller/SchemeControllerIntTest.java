package com.app.folioman.mfschemes.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
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
    void fetchSchemes() throws Exception {
        this.mockMvc
                .perform(get("/api/scheme/{schemeName}", "sbi small cap").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, is(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(jsonPath("$.size()", is(4)))
                .andExpect(jsonPath("$[*].amfiCode", contains(125494, 125495, 125496, 125497)))
                .andExpect(jsonPath("$[*].amcName", everyItem(equalTo("SBI Funds Management Limited"))));
    }
}
