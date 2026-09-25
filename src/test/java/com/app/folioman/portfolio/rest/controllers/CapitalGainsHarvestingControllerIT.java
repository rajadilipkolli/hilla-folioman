package com.app.folioman.portfolio.rest.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.app.folioman.portfolio.rest.dtos.CapitalGainsHarvestingRequestDTO;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

class CapitalGainsHarvestingControllerIT extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldReturnValidationProblemWhenPanIsInvalid() throws Exception {
        CapitalGainsHarvestingRequestDTO request = new CapitalGainsHarvestingRequestDTO(
                LocalDate.now(), null, new BigDecimal("10000"), null, null, true, true, true, null, null, null, null);

        mockMvc.perform(post("/api/portfolio/INVALID_PAN/capital-gains-harvesting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void shouldReturnValidationProblemWhenAsOfDateInFuture() throws Exception {
        CapitalGainsHarvestingRequestDTO request = new CapitalGainsHarvestingRequestDTO(
                LocalDate.now().plusDays(1),
                null,
                new BigDecimal("10000"),
                null,
                null,
                true,
                true,
                true,
                null,
                null,
                null,
                null);

        mockMvc.perform(post("/api/portfolio/ABCDE1234F/capital-gains-harvesting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Constraint Violation"))
                .andExpect(jsonPath("$.violations").exists());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    @Transactional
    void shouldReturnEmptyResultWhenNoHoldingsExist() throws Exception {
        // The PAN belongs to the user, but its folio has no holdings.
        long casId = jdbcTemplate.queryForObject("select nextval('portfolio.user_cas_details_seq')", Long.class);
        jdbcTemplate.update(
                "insert into portfolio.user_cas_details (id, cas_type, file_type) values (?, 'DETAILED', 'CAMS')",
                casId);
        jdbcTemplate.update(
                "insert into portfolio.investor_info (user_cas_details_id, email) values (?, ?)",
                casId,
                "user@test.com");
        jdbcTemplate.update(
                "insert into portfolio.user_folio_details (id, folio, amc, pan, user_cas_details_id) "
                        + "values (nextval('portfolio.user_folio_details_seq'), ?, ?, ?, ?)",
                "empty-folio",
                "Test AMC",
                "EMPTY1234A",
                casId);

        CapitalGainsHarvestingRequestDTO request = new CapitalGainsHarvestingRequestDTO(
                LocalDate.now(), null, new BigDecimal("10000"), null, null, true, true, true, null, null, null, null);

        mockMvc.perform(post("/api/portfolio/EMPTY1234A/capital-gains-harvesting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isEmpty())
                .andExpect(jsonPath("$.summary.totalStcg").value(0))
                .andExpect(jsonPath("$.summary.totalLtcg").value(0));

        mockMvc.perform(post("/api/portfolio/EMPTY1234A/capital-gains-harvesting")
                        .with(user("other@test.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
