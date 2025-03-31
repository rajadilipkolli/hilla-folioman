package com.app.folioman.common;

import com.redis.testcontainers.RedisStackContainer;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration for NoSQL and observability containers.
 * <p>
 * Manages the following test containers:
 * <ul>
 *   <li>Redis Stack - For caching and data structure operations</li>
 *   <li>MongoDB - Primary data store for event publications</li>
 *   <li>LGTM Stack - Observability stack (Loki, Grafana, Tempo, Mimir)</li>
 * </ul>
 * All containers are configured with service connection support for automatic
 * property binding in the test context.
 */
@TestConfiguration(proxyBeanMethods = false)
public class NoSQLContainersConfig {

    @Bean
    @ServiceConnection(name = "redis")
    RedisStackContainer redisStackContainer() {
        return new RedisStackContainer(RedisStackContainer.DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG))
                .withReuse(true);
    }

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo").withTag("8.0.6"));
    }

    @Bean
    @ServiceConnection
    LgtmStackContainer lgtmContainer() {
        return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:0.7.8"))
                .withStartupTimeout(Duration.ofMinutes(2));
    }
}
