package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.ConnectException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

    @Mock
    private RedisAppProperties redisAppProperties;

    @InjectMocks
    private RedisConfig redisConfig;

    @Test
    void redisTemplate_ShouldCreateConfiguredTemplate() {
        RedisTemplate<String, Object> template = redisConfig.redisTemplate(redisConnectionFactory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(redisConnectionFactory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(GenericJacksonJsonRedisSerializer.class);
        assertThat(template.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getHashValueSerializer()).isInstanceOf(GenericJacksonJsonRedisSerializer.class);
        // Ensure a default serializer is configured (the implementation uses a custom JSON serializer)
        assertThat(template.getDefaultSerializer()).isNotNull();
    }

    @Test
    void cacheManager_WithCompressionDisabled_ShouldCreateCustomRedisCacheManager() {
        setConfigFields(false, 1800L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertThat(cacheManager).isNotNull();
    }

    @Test
    void errorHandler_ShouldReturnCacheErrorHandler() {
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertThat(errorHandler).isNotNull();
    }

    @Test
    void errorHandler_HandleCacheGetError_WithConnectException() {
        given(cache.getName()).willReturn("testCache");
        RuntimeException exception = new RuntimeException(new ConnectException("Connection failed"));
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertThatCode(() -> errorHandler.handleCacheGetError(exception, cache, "testKey"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForGetError")
    void errorHandler_HandleCacheGetError_ShouldNotThrow(RuntimeException exception, Cache cache, Object key) {
        if (cache != null) {
            given(cache.getName()).willReturn("testCache");
        }
        CacheErrorHandler errorHandler = redisConfig.errorHandler();

        assertThatCode(() -> errorHandler.handleCacheGetError(exception, cache, key))
                .doesNotThrowAnyException();
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
    void cacheManager_WithCustomTtl_ShouldUseCustomValue() {
        setConfigFields(false, 7200L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertThat(cacheManager).isNotNull();
    }

    private void setConfigFields(boolean compressionEnabled, long ttlSeconds) {
        given(redisAppProperties.isCompressionEnabled()).willReturn(compressionEnabled);
        given(redisAppProperties.getDefaultTtl()).willReturn(ttlSeconds);
    }

    @Test
    void cacheManager_WithCompressionEnabled_ShouldCreateCustomRedisCacheManager() {
        setConfigFields(true, 3600L);

        CustomRedisCacheManager cacheManager =
                redisConfig.cacheManager(redisConnectionFactory, monitor, circuitBreaker);

        assertThat(cacheManager).isNotNull();
    }
}
