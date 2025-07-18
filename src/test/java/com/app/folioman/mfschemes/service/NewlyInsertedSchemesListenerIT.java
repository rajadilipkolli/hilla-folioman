package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.app.folioman.common.AbstractIntegrationTest;
import com.app.folioman.mfschemes.MFSchemeNavProjection;
import com.app.folioman.shared.UploadedSchemesList;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NewlyInsertedSchemesListenerIT extends AbstractIntegrationTest {

    @Test
    void testAsyncNavProcessingTriggeredByEvent() {
        // Given
        List<Long> schemeCodes = List.of(118272L, 120503L);
        UploadedSchemesList event = new UploadedSchemesList(schemeCodes);

        // Publish the event to trigger async processing
        applicationEventPublisher.publishEvent(event);

        // Record the time before processing to verify new NAVs
        LocalDate testStartDate = LocalDate.now().minusDays(1);

        // Then: Awaitility waits for async processing and checks repository state
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            // Verify NAVs were created for all scheme codes with recent dates
            List<MFSchemeNavProjection> navs =
                    mfSchemeNavRepository.findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                            Set.copyOf(schemeCodes), testStartDate, LocalDate.now());

            assertThat(navs).isNotEmpty();
            assertThat(navs.stream().map(nav -> nav.amfiCode()).distinct())
                    .containsExactlyInAnyOrderElementsOf(schemeCodes);
        });
    }
}
