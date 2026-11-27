package com.app.folioman.config.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.shared.AbstractIntegrationTest;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

class DbMetricsControllerIT extends AbstractIntegrationTest {

    @Test
    @WithMockUser(username = "admin")
    void poolMetrics() {

        mockMvcTester
                .get()
                .uri("/metrics/db/pool")
                .exchange()
                .assertThat()
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .convertTo(LinkedHashMap.class)
                .satisfies(linkedHashMap -> {
                    assertThat(linkedHashMap)
                            .containsKeys(
                                    "activeConnections",
                                    "idleConnections",
                                    "totalConnections",
                                    "threadsAwaitingConnection");
                });
    }
}
