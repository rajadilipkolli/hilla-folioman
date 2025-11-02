package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.ConnectException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    void cacheManager_WithCompressionDisabled_ShouldCreateCustomRedisCacheManager() {
        setConfigFields(false, 1800L);

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

    @ParameterizedTest
    @MethodSource("provideExceptionsForGetError")
    void errorHandler_HandleCacheGetError_ShouldNotThrow(RuntimeException exception, Cache cache, Object key) {
        if (cache != null) {
            when(cache.getName()).thenReturn("testCache");
        }
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(exception, cache, key));
    }

    private static Stream<Arguments> provideExceptionsForGetError() {
        Cache mockCache = mock(Cache.class);
        return Stream.of(
                Arguments.of(new RuntimeException(new ConnectException("Connection failed")), mockCache, "testKey"),
                Arguments.of(new RuntimeException("General error"), mockCache, "testKey"),
                Arguments.of(new RuntimeException("Error"), null, "testKey"),
                Arguments.of(new RuntimeException("Error"), mockCache, null));
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
        setConfigFields(false, 7200L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertNotNull(cacheManager);
    }

    private void setConfigFields(boolean compressionEnabled, long ttlSeconds) {
        ReflectionTestUtils.setField(redisConfig, "compressionEnabled", compressionEnabled);
        ReflectionTestUtils.setField(redisConfig, "defaultTtlSeconds", ttlSeconds);
    }

    @Test
    void cacheManager_WithCompressionEnabled_ShouldCreateCustomRedisCacheManager() {
        setConfigFields(true, 3600L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertNotNull(cacheManager);
    }
}
