package com.app.folioman.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        name = "spring.modulith.events.mongodb.transaction-management.enabled",
        havingValue = "true",
        matchIfMissing = true)
class MongoDbConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }
}
