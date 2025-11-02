package com.app.folioman.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;

class AuditConfigurationTest {

    private AuditConfiguration auditConfiguration;

    @BeforeEach
    void setUp() {
        auditConfiguration = new AuditConfiguration();
    }

    @Test
    void testAuditorAware_ReturnsNonNullAuditorAware() {
        AuditorAware<String> auditorAware = auditConfiguration.auditorAware();

        assertNotNull(auditorAware);
    }

    @Test
    void testAuditorAware_ReturnsExpectedAuditorValue() {
        AuditorAware<String> auditorAware = auditConfiguration.auditorAware();
        Optional<String> currentAuditor = auditorAware.getCurrentAuditor();

        assertTrue(currentAuditor.isPresent());
        assertEquals("App", currentAuditor.get());
    }

    @Test
    void testAuditorAware_ReturnsOptionalWithValue() {
        AuditorAware<String> auditorAware = auditConfiguration.auditorAware();
        Optional<String> currentAuditor = auditorAware.getCurrentAuditor();

        assertTrue(currentAuditor.isPresent());
    }
}
