package com.app.folioman.mfschemes.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.app.folioman.shared.AbstractIntegrationTest;
import com.app.folioman.shared.UploadedSchemesList;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewlyInsertedSchemesListenerIT extends AbstractIntegrationTest {

    @Test
    void asyncNavProcessingTriggeredByEvent() {
        // Given
        List<Long> schemeCodes = List.of(118272L, 120503L);
        UploadedSchemesList event = new UploadedSchemesList(schemeCodes);

        // Publish the event to trigger async processing within a transaction
        // so that the @ApplicationModuleListener (which is a @TransactionalEventListener) fires after commit.
        transactionTemplate.executeWithoutResult(status -> {
            applicationEventPublisher.publishEvent(event);
        });

        // Record the time before processing to verify new NAVs
        // Widen window to 7 days to include NAVs that may have older navDate values due
        // to holidays
        LocalDate testStartDate = LocalDate.now().minusDays(7);

        // Then: Awaitility waits for async processing and checks repository state
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            // Verify NAVs were created for all scheme codes with recent dates
            String sql = "SELECT s.amfi_code FROM mfschemes.mf_scheme_nav n "
                    + "JOIN mfschemes.mf_fund_scheme s ON n.mf_scheme_id = s.id "
                    + "WHERE s.amfi_code IN (?, ?) "
                    + "AND n.nav_date >= ? AND n.nav_date <= ?";

            List<Long> amfiCodes = jdbcTemplate.query(
                    sql, (rs, rowNum) -> rs.getLong("amfi_code"), 118272L, 120503L, testStartDate, LocalDate.now());

            assertThat(amfiCodes).isNotEmpty().hasSizeGreaterThan(3);
            assertThat(amfiCodes.stream().distinct()).containsExactlyInAnyOrderElementsOf(schemeCodes);
        });
    }
}
