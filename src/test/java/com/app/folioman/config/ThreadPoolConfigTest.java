package com.app.folioman.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class ThreadPoolConfigTest {

    private ThreadPoolConfig threadPoolConfig;

    @BeforeEach
    void setUp() {
        threadPoolConfig = new ThreadPoolConfig();
    }

    @AfterEach
    void tearDown() {
        if (threadPoolConfig != null) {
            TaskExecutor executor = threadPoolConfig.taskExecutor();
            if (executor instanceof ThreadPoolTaskExecutor taskExecutor) {
                taskExecutor.shutdown();
            }
        }
    }

    @Test
    void taskExecutor_ShouldCreateAndConfigureThreadPoolTaskExecutor() {
        TaskExecutor result = threadPoolConfig.taskExecutor();

        assertThat(result).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) result;
        assertThat(executor.getThreadPoolExecutor()).isNotNull();
        assertThat(executor.getCorePoolSize()).isEqualTo(5);
        assertThat(executor.getMaxPoolSize()).isEqualTo(10);
        assertThat(executor.getQueueCapacity()).isEqualTo(25);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("AsyncFolioman-");
    }
}
