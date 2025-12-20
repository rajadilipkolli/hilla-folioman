package com.app.folioman.config;

import static org.assertj.core.api.Assertions.assertThat;

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
    void auditorAwareReturnsNonNullAuditorAware() {
        AuditorAware<String> auditorAware = auditConfiguration.auditorAware();

        assertThat(auditorAware).isNotNull();
    }

    @Test
    void auditorAwareReturnsExpectedAuditorValue() {
        AuditorAware<String> auditorAware = auditConfiguration.auditorAware();
        Optional<String> currentAuditor = auditorAware.getCurrentAuditor();

        assertThat(currentAuditor).isPresent();
        assertThat(currentAuditor).hasValue("App");
    }
}
