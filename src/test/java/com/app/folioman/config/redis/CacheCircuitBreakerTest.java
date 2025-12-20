package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;

@ExtendWith(MockitoExtension.class)
class CacheCircuitBreakerTest {

    private CacheCircuitBreaker cacheCircuitBreaker;

    @BeforeEach
    void setUp() {
        cacheCircuitBreaker = new CacheCircuitBreaker(50.0f, 30, 100);
    }

    @Test
    void constructor_WithDefaultValues_ShouldCreateCircuitBreaker() {
        CacheCircuitBreaker circuitBreaker = new CacheCircuitBreaker(50.0f, 30, 100);

        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void constructor_WithCustomValues_ShouldCreateCircuitBreaker() {
        CacheCircuitBreaker circuitBreaker = new CacheCircuitBreaker(75.0f, 60, 200);

        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void executeWithFallback_WhenSupplierSucceeds_ShouldReturnSupplierResult() {
        Supplier<String> supplier = () -> "success";
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void executeWithFallback_WhenSupplierThrowsException_ShouldReturnFallbackResult() {
        Supplier<String> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void executeWithFallback_WhenSupplierThrowsQueryTimeoutException_ShouldReturnFallbackResult() {
        Supplier<String> supplier = () -> {
            throw new QueryTimeoutException("Query timeout");
        };
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void executeWithFallback_WhenSupplierThrowsDataAccessResourceFailureException_ShouldReturnFallbackResult() {
        Supplier<String> supplier = () -> {
            throw new DataAccessResourceFailureException("Data access failure");
        };
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertThat(result).isEqualTo("fallback");
    }

    @Test
    void executeWithFallback_WithIntegerType_ShouldWork() {
        Supplier<Integer> supplier = () -> 42;
        Supplier<Integer> fallback = () -> 0;

        Integer result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertThat(result).isEqualTo(42);
    }

    @Test
    void execute_WhenSupplierSucceeds_ShouldReturnSupplierResult() {
        Supplier<String> supplier = () -> "success";

        String result = cacheCircuitBreaker.execute(supplier);

        assertThat(result).isEqualTo("success");
    }

    @Test
    void execute_WhenSupplierThrowsException_ShouldReturnNull() {
        Supplier<String> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };

        String result = cacheCircuitBreaker.execute(supplier);

        assertThat(result).isNull();
    }

    @Test
    void execute_WithIntegerType_WhenSupplierSucceeds_ShouldReturnResult() {
        Supplier<Integer> supplier = () -> 100;

        Integer result = cacheCircuitBreaker.execute(supplier);

        assertThat(result).isEqualTo(100);
    }

    @Test
    void execute_WithIntegerType_WhenSupplierThrowsException_ShouldReturnNull() {
        Supplier<Integer> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };

        Integer result = cacheCircuitBreaker.execute(supplier);

        assertThat(result).isNull();
    }

    @Test
    void getState_ShouldReturnCurrentCircuitBreakerState() {
        CircuitBreaker.State state = cacheCircuitBreaker.getState();

        assertThat(state).isNotNull();
        assertThat(state).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void executeWithFallback_WhenFallbackThrowsException_ShouldPropagateException() {
        Supplier<String> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };
        Supplier<String> fallback = () -> {
            throw new RuntimeException("Fallback failed");
        };

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> cacheCircuitBreaker.executeWithFallback(supplier, fallback));
    }

    @Test
    void executeWithFallback_WithNullSupplier_ShouldThrowException() {
        Supplier<String> fallback = () -> "fallback";

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> cacheCircuitBreaker.executeWithFallback(null, fallback));
    }

    @Test
    void execute_WithNullSupplier_ShouldThrowException() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> cacheCircuitBreaker.execute(null));
    }
}
