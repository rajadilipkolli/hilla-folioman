package com.example.application.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing
public class AuditConfiguration {

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> Optional.of("App");
    }
}
