package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        assertNotNull(circuitBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void constructor_WithCustomValues_ShouldCreateCircuitBreaker() {
        CacheCircuitBreaker circuitBreaker = new CacheCircuitBreaker(75.0f, 60, 200);

        assertNotNull(circuitBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void executeWithFallback_WhenSupplierSucceeds_ShouldReturnSupplierResult() {
        Supplier<String> supplier = () -> "success";
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertEquals("success", result);
    }

    @Test
    void executeWithFallback_WhenSupplierThrowsException_ShouldReturnFallbackResult() {
        Supplier<String> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertEquals("fallback", result);
    }

    @Test
    void executeWithFallback_WhenSupplierThrowsQueryTimeoutException_ShouldReturnFallbackResult() {
        Supplier<String> supplier = () -> {
            throw new QueryTimeoutException("Query timeout");
        };
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertEquals("fallback", result);
    }

    @Test
    void executeWithFallback_WhenSupplierThrowsDataAccessResourceFailureException_ShouldReturnFallbackResult() {
        Supplier<String> supplier = () -> {
            throw new DataAccessResourceFailureException("Data access failure");
        };
        Supplier<String> fallback = () -> "fallback";

        String result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertEquals("fallback", result);
    }

    @Test
    void executeWithFallback_WithIntegerType_ShouldWork() {
        Supplier<Integer> supplier = () -> 42;
        Supplier<Integer> fallback = () -> 0;

        Integer result = cacheCircuitBreaker.executeWithFallback(supplier, fallback);

        assertEquals(42, result);
    }

    @Test
    void execute_WhenSupplierSucceeds_ShouldReturnSupplierResult() {
        Supplier<String> supplier = () -> "success";

        String result = cacheCircuitBreaker.execute(supplier);

        assertEquals("success", result);
    }

    @Test
    void execute_WhenSupplierThrowsException_ShouldReturnNull() {
        Supplier<String> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };

        String result = cacheCircuitBreaker.execute(supplier);

        assertNull(result);
    }

    @Test
    void execute_WithIntegerType_WhenSupplierSucceeds_ShouldReturnResult() {
        Supplier<Integer> supplier = () -> 100;

        Integer result = cacheCircuitBreaker.execute(supplier);

        assertEquals(100, result);
    }

    @Test
    void execute_WithIntegerType_WhenSupplierThrowsException_ShouldReturnNull() {
        Supplier<Integer> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };

        Integer result = cacheCircuitBreaker.execute(supplier);

        assertNull(result);
    }

    @Test
    void getState_ShouldReturnCurrentCircuitBreakerState() {
        CircuitBreaker.State state = cacheCircuitBreaker.getState();

        assertNotNull(state);
        assertEquals(CircuitBreaker.State.CLOSED, state);
    }

    @Test
    void executeWithFallback_WhenFallbackThrowsException_ShouldPropagateException() {
        Supplier<String> supplier = () -> {
            throw new RedisConnectionFailureException("Redis connection failed");
        };
        Supplier<String> fallback = () -> {
            throw new RuntimeException("Fallback failed");
        };

        assertThrows(RuntimeException.class, () -> {
            cacheCircuitBreaker.executeWithFallback(supplier, fallback);
        });
    }

    @Test
    void executeWithFallback_WithNullSupplier_ShouldThrowException() {
        Supplier<String> fallback = () -> "fallback";

        assertThrows(NullPointerException.class, () -> {
            cacheCircuitBreaker.executeWithFallback(null, fallback);
        });
    }

    @Test
    void execute_WithNullSupplier_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            cacheCircuitBreaker.execute(null);
        });
    }
}
