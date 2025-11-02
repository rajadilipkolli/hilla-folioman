package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializer;

@ExtendWith(MockitoExtension.class)
class CustomRedisCacheManagerTest {

    @Mock
    private RedisCacheWriter redisCacheWriter;

    @Mock
    private Monitor monitor;

    @Mock
    private CacheCircuitBreaker circuitBreaker;

    @Mock
    private RedisCacheConfiguration redisCacheConfiguration;

    @Mock
    private RedisSerializer<Object> redisSerializer;

    private CustomRedisCacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Duration defaultTtl = Duration.ofMinutes(5);
        cacheManager = new CustomRedisCacheManager(redisCacheWriter, monitor, circuitBreaker, defaultTtl);
    }

    @Test
    void constructorWithNonNullDefaultTtl() {
        Duration customTtl = Duration.ofMinutes(15);

        try (MockedStatic<RedisSerializer> mockedRedisSerializer = mockStatic(RedisSerializer.class)) {
            mockedRedisSerializer.when(RedisSerializer::java).thenReturn(redisSerializer);

            CustomRedisCacheManager manager =
                    new CustomRedisCacheManager(redisCacheWriter, monitor, circuitBreaker, customTtl);

            assertNotNull(manager);
            mockedRedisSerializer.verify(RedisSerializer::java);
        }
    }

    @Test
    void constructorWithNullDefaultTtl() {
        try (MockedStatic<RedisSerializer> mockedRedisSerializer = mockStatic(RedisSerializer.class)) {
            mockedRedisSerializer.when(RedisSerializer::java).thenReturn(redisSerializer);

            CustomRedisCacheManager manager =
                    new CustomRedisCacheManager(redisCacheWriter, monitor, circuitBreaker, null);

            assertNotNull(manager);
            mockedRedisSerializer.verify(RedisSerializer::java);
        }
    }

    @Test
    void createRedisCacheWithNullCacheConfig() {
        String cacheName = "testCache";

        try (MockedStatic<RedisSerializer> mockedRedisSerializer = mockStatic(RedisSerializer.class)) {
            mockedRedisSerializer.when(RedisSerializer::java).thenReturn(redisSerializer);

            CustomRedisCache result = (CustomRedisCache) cacheManager.createRedisCache(cacheName, null);

            assertNotNull(result);
        }
    }

    @Test
    void createRedisCacheWithCacheConfigHavingNullTtl() {
        String cacheName = "testCache";
        when(redisCacheConfiguration.getTtl()).thenReturn(null);

        try (MockedStatic<RedisSerializer> mockedRedisSerializer = mockStatic(RedisSerializer.class)) {
            mockedRedisSerializer.when(RedisSerializer::java).thenReturn(redisSerializer);

            CustomRedisCache result =
                    (CustomRedisCache) cacheManager.createRedisCache(cacheName, redisCacheConfiguration);

            assertNotNull(result);
            verify(redisCacheConfiguration).getTtl();
        }
    }

    @Test
    void createRedisCacheWithValidCacheConfigTtl() {
        String cacheName = "testCache";
        Duration configTtl = Duration.ofMinutes(20);
        when(redisCacheConfiguration.getTtl()).thenReturn(configTtl);

        try (MockedStatic<RedisSerializer> mockedRedisSerializer = mockStatic(RedisSerializer.class)) {
            mockedRedisSerializer.when(RedisSerializer::java).thenReturn(redisSerializer);

            CustomRedisCache result =
                    (CustomRedisCache) cacheManager.createRedisCache(cacheName, redisCacheConfiguration);

            assertNotNull(result);
            verify(redisCacheConfiguration).getTtl();
        }
    }
}
