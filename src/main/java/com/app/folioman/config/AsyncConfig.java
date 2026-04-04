package com.app.folioman.config;

import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfig.class);

    // When using @Async methods or AsyncTaskExecutor, context gets lost in new threads. Fix this by configuring the
    // ContextPropagatingTaskDecorator
    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    @Bean("virtualThreadExecutor")
    Executor virtualThreadExecutor() {
        // Create a custom virtual thread executor with naming and context propagation
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        executor.setThreadNamePrefix("AsyncFolioman-");
        executor.setTaskDecorator(contextPropagatingTaskDecorator());
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return virtualThreadExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            LOGGER.error("Exception in @Async method: {}", method.getName(), ex);
        };
    }
}
