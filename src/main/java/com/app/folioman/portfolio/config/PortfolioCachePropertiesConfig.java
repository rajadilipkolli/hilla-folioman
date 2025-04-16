package com.app.folioman.portfolio.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable portfolio cache properties.
 * This follows the pattern of separating the properties configuration from the actual usage.
 */
@Configuration
@EnableConfigurationProperties(PortfolioCacheProperties.class)
public class PortfolioCachePropertiesConfig {
    // No additional beans needed, just enabling the properties
}
