package com.app.folioman.config.redis;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides circuit breaker functionality for Redis cache operations.
 * Prevents cascading failures when Redis is experiencing issues.
 */
@Component
public class CacheCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(CacheCircuitBreaker.class);
    private final CircuitBreaker circuitBreaker;

    public CacheCircuitBreaker(
            @Value("${app.cache.circuit-breaker.failure-rate-threshold:50}") float failureRateThreshold,
            @Value("${app.cache.circuit-breaker.wait-duration-seconds:30}") int waitDurationSeconds,
            @Value("${app.cache.circuit-breaker.sliding-window-size:100}") int slidingWindowSize) {

        // Configure the circuit breaker with sensible defaults
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationSeconds))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(slidingWindowSize)
                .permittedNumberOfCallsInHalfOpenState(10)
                .recordExceptions(
                        org.springframework.dao.QueryTimeoutException.class,
                        org.springframework.dao.DataAccessResourceFailureException.class,
                        org.springframework.data.redis.RedisConnectionFailureException.class,
                        java.net.ConnectException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        this.circuitBreaker = registry.circuitBreaker("redisCache");

        // Register event handlers
        this.circuitBreaker
                .getEventPublisher()
                .onStateTransition(event -> {
                    log.warn(
                            "Redis circuit breaker state changed from {} to {}",
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                })
                .onError(event -> {
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "Redis circuit breaker recorded error: {}",
                                event.getThrowable().getMessage());
                    }
                });
    }

    /**
     * Execute a Redis operation with circuit breaker protection
     *
     * @param <T> the type of the result
     * @param supplier the operation to execute
     * @param fallback the fallback operation to use when the circuit is open
     * @return the result of the operation or fallback
     */
    public <T> T executeWithFallback(Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, supplier).get();
        } catch (Exception e) {
            log.warn("Redis operation failed, using fallback. Error: {}", e.getMessage());
            return fallback.get();
        }
    }

    /**
     * Execute a Redis operation with circuit breaker protection
     *
     * @param <T> the type of the result
     * @param supplier the operation to execute
     * @return the result of the operation or null if the circuit is open
     */
    public <T> T execute(Supplier<T> supplier) {
        return executeWithFallback(supplier, () -> null);
    }

    /**
     * @return the current state of the circuit breaker
     */
    public CircuitBreaker.State getState() {
        return circuitBreaker.getState();
    }
}
