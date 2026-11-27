package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.NavNotFoundException;
import com.app.folioman.mfschemes.config.ApplicationProperties;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeDTO;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class MFNavServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestClient restClient;

    private MFNavServiceImpl mfNavService;

    @BeforeEach
    void setUp() {
        mfNavService = new MFNavServiceImpl(jdbcTemplate, new ApplicationProperties(), restClient);
    }

    @Test
    void usesStoredNavWithoutCallingExternalApi() {
        LocalDate date = LocalDate.of(2024, 4, 12);
        MFSchemeDTO stored = new MFSchemeDTO("AMC", 123456L, "ISIN", "Fund", "432100", "2024-04-11", "Equity");
        when(jdbcTemplate.query(
                        anyString(),
                        ArgumentMatchers.<RowMapper<MFSchemeDTO>>any(),
                        eq(Date.valueOf(date)),
                        eq(123456L)))
                .thenReturn(List.of(stored));

        assertThat(mfNavService.getNavOnDate(123456L, date))
                .isEqualTo(new MFSchemeDTO("AMC", 123456L, "ISIN", "Fund", "43.21000", "2024-04-11", "Equity"));
        verifyNoInteractions(restClient);
    }

    @Test
    void reportsMissingSchemeWithoutCallingExternalApi() {
        LocalDate date = LocalDate.of(2024, 4, 12);
        when(jdbcTemplate.query(
                        anyString(),
                        ArgumentMatchers.<RowMapper<MFSchemeDTO>>any(),
                        eq(Date.valueOf(date)),
                        eq(123456L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> mfNavService.getNavOnDate(123456L, date)).isInstanceOf(NavNotFoundException.class);
        verifyNoInteractions(restClient);
    }

    @Test
    void emptyBatchSkipsDatabaseLookup() {
        LocalDate date = LocalDate.of(2024, 4, 12);

        assertThat(mfNavService.getNavsForSchemesAndDates(Set.of(), date, date)).isEmpty();
        assertThat(mfNavService.getLastTwoNavsForSchemes(Set.of())).isEmpty();
        verifyNoInteractions(jdbcTemplate);
    }
}
