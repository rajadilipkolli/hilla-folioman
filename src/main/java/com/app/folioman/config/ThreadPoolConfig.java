package com.app.folioman.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
public class ThreadPoolConfig {

    @Bean(name = "taskExecutor")
    TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Initial number of threads
        executor.setMaxPoolSize(10); // Maximum number of threads
        executor.setQueueCapacity(25); // Queue capacity for waiting tasks
        executor.setThreadNamePrefix("AsyncSupabase-");
        executor.initialize();
        return executor;
    }
}
