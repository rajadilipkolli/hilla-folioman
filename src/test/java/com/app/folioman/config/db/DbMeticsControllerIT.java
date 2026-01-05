package com.app.folioman.config.db;

import static org.junit.jupiter.api.Assertions.*;

import com.app.folioman.shared.AbstractIntegrationTest;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DbMeticsControllerIT extends AbstractIntegrationTest {

    @Test
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
                    assertTrue(linkedHashMap.containsKey("activeConnections"));
                    assertTrue(linkedHashMap.containsKey("idleConnections"));
                    assertTrue(linkedHashMap.containsKey("totalConnections"));
                    assertTrue(linkedHashMap.containsKey("threadsAwaitingConnection"));
                });
    }
}
