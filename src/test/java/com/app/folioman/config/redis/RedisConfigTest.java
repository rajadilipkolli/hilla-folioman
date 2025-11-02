package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.ConnectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private Monitor monitor;

    @Mock
    private CacheCircuitBreaker circuitBreaker;

    @Mock
    private Cache cache;

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
    }

    @Test
    void redisTemplate_ShouldCreateConfiguredTemplate() {
        RedisTemplate<String, Object> template = redisConfig.redisTemplate(redisConnectionFactory);

        assertNotNull(template);
        assertEquals(redisConnectionFactory, template.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, template.getValueSerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashKeySerializer());
        assertInstanceOf(GenericJackson2JsonRedisSerializer.class, template.getHashValueSerializer());
        assertTrue(template.isEnableDefaultSerializer());
    }

    @Test
    void cacheManager_WithCompressionEnabled_ShouldCreateCustomRedisCacheManager() {
        ReflectionTestUtils.setField(redisConfig, "compressionEnabled", true);
        ReflectionTestUtils.setField(redisConfig, "defaultTtlSeconds", 3600L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertNotNull(cacheManager);
    }

    @Test
    void cacheManager_WithCompressionDisabled_ShouldCreateCustomRedisCacheManager() {
        ReflectionTestUtils.setField(redisConfig, "compressionEnabled", false);
        ReflectionTestUtils.setField(redisConfig, "defaultTtlSeconds", 1800L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertNotNull(cacheManager);
    }

    @Test
    void errorHandler_ShouldReturnCacheErrorHandler() {
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertNotNull(errorHandler);
    }

    @Test
    void errorHandler_HandleCacheGetError_WithConnectException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException(new ConnectException("Connection failed"));
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(exception, cache, "testKey"));
    }

    @Test
    void errorHandler_HandleCacheGetError_WithOtherException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException("General error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(exception, cache, "testKey"));
    }

    @Test
    void errorHandler_HandleCacheGetError_WithNullCache() {
        RuntimeException exception = new RuntimeException("Error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(exception, null, "testKey"));
    }

    @Test
    void errorHandler_HandleCacheGetError_WithNullKey() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException("Error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(exception, cache, null));
    }

    @Test
    void errorHandler_HandleCachePutError_WithConnectException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException(new ConnectException("Connection failed"));
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCachePutError(exception, cache, "testKey", "testValue"));
    }

    @Test
    void errorHandler_HandleCachePutError_WithOtherException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException("General error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCachePutError(exception, cache, "testKey", "testValue"));
    }

    @Test
    void errorHandler_HandleCachePutError_WithNullValue() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException("Error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCachePutError(exception, cache, "testKey", null));
    }

    @Test
    void errorHandler_HandleCacheEvictError_WithConnectException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException(new ConnectException("Connection failed"));
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(exception, cache, "testKey"));
    }

    @Test
    void errorHandler_HandleCacheEvictError_WithOtherException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException("General error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(exception, cache, "testKey"));
    }

    @Test
    void errorHandler_HandleCacheClearError_WithConnectException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException(new ConnectException("Connection failed"));
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(exception, cache));
    }

    @Test
    void errorHandler_HandleCacheClearError_WithOtherException() {
        when(cache.getName()).thenReturn("testCache");
        RuntimeException exception = new RuntimeException("General error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(exception, cache));
    }

    @Test
    void errorHandler_HandleCacheClearError_WithNullCache() {
        RuntimeException exception = new RuntimeException("Error");
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(exception, null));
    }

    @Test
    void defaultFieldValues_ShouldBeSetCorrectly() {
        RedisConfig config = new RedisConfig();

        boolean compressionEnabled = (boolean) ReflectionTestUtils.getField(config, "compressionEnabled");
        Long defaultTtlSeconds = (Long) ReflectionTestUtils.getField(config, "defaultTtlSeconds");

        assertFalse(compressionEnabled);
        assertNotNull(defaultTtlSeconds);
    }

    @Test
    void cacheManager_WithCustomTtl_ShouldUseCustomValue() {
        ReflectionTestUtils.setField(redisConfig, "compressionEnabled", false);
        ReflectionTestUtils.setField(redisConfig, "defaultTtlSeconds", 7200L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertNotNull(cacheManager);
    }
}
