package com.app.folioman.common;

import com.redis.testcontainers.RedisStackContainer;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class RedisContainersConfig {

    @Bean
    @ServiceConnection(name = "redis")
    @RestartScope
    RedisStackContainer redisStackContainer() {
        return new RedisStackContainer(RedisStackContainer.DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG))
                .withReuse(true);
    }
}
