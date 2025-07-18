package com.app.folioman.mfschemes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.app.folioman.common.AbstractIntegrationTest;
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
        List<Long> schemeCodes = List.of(120503L, 118272L, 118968L);
        UploadedSchemesList event = new UploadedSchemesList(schemeCodes);

        // Publish the event to trigger async processing
        eventPublisher.publishEvent(event);

        // Then: Awaitility waits for async processing and checks repository state
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            for (Long code : schemeCodes) {
                // Check that at least one NAV exists for each scheme code
                boolean navExists = !mfSchemeNavRepository
                        .findByMfScheme_AmfiCodeInAndNavDateGreaterThanEqualAndNavDateLessThanEqual(
                                Set.of(code), LocalDate.of(2000, 1, 1), LocalDate.now())
                        .isEmpty();
                assertThat(navExists)
                        .as("NAV should exist for scheme code " + code)
                        .isTrue();
            }
        });
    }
}
