package com.app.folioman.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.ConnectException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

@Configuration(proxyBeanMethods = false)
@EnableCaching
@ConfigurationPropertiesScan
class RedisConfig implements CachingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

    private final RedisAppProperties redisAppProperties;

    RedisConfig(RedisAppProperties redisAppProperties) {
        this.redisAppProperties = redisAppProperties;
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());

        // Configure value serializer based on complexity/size of objects
        template.setValueSerializer(createOptimizedSerializer());

        // Configure hash operations
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(createOptimizedSerializer());

        // For better performance, enable transaction support only if needed
        template.setEnableTransactionSupport(false);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    CustomRedisCacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory, Monitor monitor, CacheCircuitBreaker circuitBreaker) {

        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);

        // Create default cache configuration with compression if enabled
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisAppProperties.getDefaultTtl()))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

        // Configure serialization based on compression setting
        if (redisAppProperties.isCompressionEnabled()) {
            cacheConfiguration =
                    cacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                            new CompressedRedisSerializer<>(createOptimizedSerializer())));
            LOGGER.info("Redis value compression enabled for values over 1KB");
        } else {
            cacheConfiguration = cacheConfiguration.serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(createOptimizedSerializer()));
            LOGGER.info("Redis value compression disabled");
        }

        // Create the custom cache manager with our circuit breaker and default TTL
        return new CustomRedisCacheManager(
                redisCacheWriter, monitor, circuitBreaker, Duration.ofSeconds(redisAppProperties.getDefaultTtl()));
    }

    /**
     * Creates an optimized serializer for Redis values
     */
    private RedisSerializer<Object> createOptimizedSerializer() {
        // Create a custom ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        // Use JSON serialization for better memory efficiency and readability
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("get", cache, key, null, exception);
            }

            @Override
            public void handleCachePutError(
                    RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
                logCacheError("put", cache, key, value, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logCacheError("evict", cache, key, null, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logCacheError("clear", cache, null, null, exception);
            }

            private void logCacheError(
                    String operation, Cache cache, Object key, @Nullable Object value, Exception exception) {
                String cacheName = cache != null ? cache.getName() : "unknown";
                String keyString = key != null ? key.toString() : "null";

                if (exception.getCause() instanceof ConnectException) {
                    LOGGER.warn(
                            "Redis connection issue during {} operation. Cache: {}, Key: {}",
                            operation,
                            cacheName,
                            keyString);
                } else {
                    LOGGER.error("Cache {} error. Operation: {}, Key: {}", cacheName, operation, keyString, exception);
                }
            }
        };
    }
}
