package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.mfschemes.MfSchemeService;
import com.app.folioman.mfschemes.rest.dtos.FundDetailProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeNavProjection;
import com.app.folioman.mfschemes.rest.dtos.MFSchemeProjection;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SchemeJdbcLookupIT extends AbstractIntegrationTest {

    @Autowired
    private MFNavService navService;

    @Autowired
    private MfSchemeService schemeService;

    @Test
    void looksUpDatedAndLatestNavsAcrossSchemes() {
        jdbcTemplate.update("""
                INSERT INTO nav (scheme_code, date, nav) VALUES
                    (991001, DATE '2024-04-10', 100000),
                    (991001, DATE '2024-04-11', 120000),
                    (991001, DATE '2024-04-12', 123456),
                    (991002, DATE '2024-04-11', 90000)
                """);
        var codes = Set.of(991001L, 991002L, 991003L);
        LocalDate start = LocalDate.of(2024, 4, 11);
        LocalDate end = LocalDate.of(2024, 4, 12);

        var byDate = navService.getNavsForSchemesAndDates(codes, start, end);
        assertThat(byDate).containsOnlyKeys(991001L, 991002L);
        assertThat(byDate.get(991001L)).containsOnlyKeys(start, end);
        assertThat(byDate.get(991002L)).containsOnlyKeys(start);
        assertThat(byDate.get(991001L).get(end).nav()).isEqualByComparingTo(new BigDecimal("12.3456"));
        assertThat(navService.getNavsForSchemesAndDates(codes, end.plusDays(1), end.plusDays(1)))
                .isEmpty();

        var latest = navService.getLastTwoNavsForSchemes(codes);
        assertThat(latest).containsOnlyKeys(991001L, 991002L);
        assertThat(latest.get(991001L))
                .extracting(MFSchemeNavProjection::navDate)
                .containsExactly(end, start);
        assertThat(latest.get(991002L))
                .extracting(MFSchemeNavProjection::navDate)
                .containsExactly(start);
    }

    @Test
    void keepsOneSchemeProjectionAndLooksUpNumericQueriesByCodeOnly() {
        jdbcTemplate.update("""
                INSERT INTO schemes (scheme_code, scheme_name, plan, category, amc) VALUES
                    (991001, 'Example Fund', 'Direct', 'Equity', 'Example AMC'),
                    (991002, 'Fund 991001', 'Regular', 'Equity', 'Another AMC')
                """);
        jdbcTemplate.update("""
                INSERT INTO securities (scheme_code, isin, type) VALUES
                    (991001, 'ZZZ', 1),
                    (991001, 'AAA', 2)
                """);

        assertThat(schemeService.findByAmfiCode(991001L))
                .get()
                .extracting(MFSchemeProjection::getIsin)
                .isEqualTo("AAA");
        var batch = schemeService.findByAmfiCodeIn(List.of(991001L, 991002L, 991001L));
        assertThat(batch).extracting(MFSchemeProjection::getAmfiCode).containsExactlyInAnyOrder(991001L, 991002L);
        assertThat(batch).extracting(MFSchemeProjection::getIsin).containsExactlyInAnyOrder("AAA", null);
        assertThat(schemeService.fetchSchemes(" 991001 "))
                .extracting(FundDetailProjection::getAmfiCode)
                .containsExactly(991001L);
        assertThat(schemeService.fetchSchemes("  ")).isEmpty();
    }
}
