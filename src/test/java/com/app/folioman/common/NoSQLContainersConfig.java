package com.app.folioman.common;

import com.redis.testcontainers.RedisStackContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

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
        return new MongoDBContainer(DockerImageName.parse("mongo").withTag("8.0.3"));
    }
}
