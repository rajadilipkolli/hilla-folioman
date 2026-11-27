package com.app.folioman.config;

import com.vaadin.flow.spring.annotation.VaadinTaskExecutor;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * Configuration for asynchronous method execution using virtual threads.
 * Ensures context propagation across async boundaries and provides exception handling.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Creates a task decorator that propagates context to async threads.
     * Necessary to maintain context when using @Async methods or AsyncTaskExecutor.
     *
     * @return A ContextPropagatingTaskDecorator instance
     */
    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    /**
     * Creates a virtual thread executor with context propagation support.
     * Configures thread naming and decorates tasks to preserve context.
     *
     * @return An Executor configured for virtual threads
     */
    @Bean("virtualThreadExecutor")
    @VaadinTaskExecutor
    Executor virtualThreadExecutor() {
        // Create a custom virtual thread executor with naming and context propagation
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setVirtualThreads(true);
        executor.setThreadNamePrefix("AsyncFolioman-");
        executor.setTaskDecorator(contextPropagatingTaskDecorator());
        return executor;
    }

    /**
     * Returns the default async executor for @Async annotated methods.
     *
     * @return The virtual thread executor
     */
    @Override
    public Executor getAsyncExecutor() {
        return virtualThreadExecutor();
    }

    /**
     * Returns the exception handler for uncaught exceptions in async methods.
     *
     * @return An AsyncUncaughtExceptionHandler that logs exceptions
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            LOGGER.error("Exception in @Async method: {}", method.getName(), ex);
        };
    }
}
