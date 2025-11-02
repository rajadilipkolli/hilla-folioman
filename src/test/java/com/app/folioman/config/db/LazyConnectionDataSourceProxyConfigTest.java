package com.app.folioman.config.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@ExtendWith(MockitoExtension.class)
class LazyConnectionDataSourceProxyConfigTest {

    @Mock
    private ProxyDataSource mockProxyDataSource;

    private BeanPostProcessor beanPostProcessor;

    @BeforeEach
    void setUp() {
        beanPostProcessor = LazyConnectionDataSourceProxyConfig.lazyConnectionDataSourceProxyBeanPostProcessor();
    }

    @Test
    void lazyConnectionDataSourceProxyBeanPostProcessor_ShouldReturnBeanPostProcessor() {
        BeanPostProcessor result = LazyConnectionDataSourceProxyConfig.lazyConnectionDataSourceProxyBeanPostProcessor();

        assertNotNull(result);
        assertInstanceOf(BeanPostProcessor.class, result);
    }

    @Test
    void
            postProcessAfterInitialization_WithProxyDataSourceAndNotScopedTarget_ShouldReturnLazyConnectionDataSourceProxy() {
        String beanName = "testBean";

        try (MockedStatic<ScopedProxyUtils> mockedScopedProxyUtils = mockStatic(ScopedProxyUtils.class)) {
            mockedScopedProxyUtils
                    .when(() -> ScopedProxyUtils.isScopedTarget(beanName))
                    .thenReturn(false);

            Object result = beanPostProcessor.postProcessAfterInitialization(mockProxyDataSource, beanName);

            assertNotNull(result);
            assertInstanceOf(LazyConnectionDataSourceProxy.class, result);
        }
    }

    @Test
    void postProcessAfterInitialization_WithProxyDataSourceAndScopedTarget_ShouldReturnOriginalBean() {
        String beanName = "scopedTarget.testBean";

        try (MockedStatic<ScopedProxyUtils> mockedScopedProxyUtils = mockStatic(ScopedProxyUtils.class)) {
            mockedScopedProxyUtils
                    .when(() -> ScopedProxyUtils.isScopedTarget(beanName))
                    .thenReturn(true);

            Object result = beanPostProcessor.postProcessAfterInitialization(mockProxyDataSource, beanName);

            assertSame(mockProxyDataSource, result);
        }
    }

    @Test
    void postProcessAfterInitialization_WithNonProxyDataSource_ShouldReturnOriginalBean() {
        Object nonProxyDataSource = new Object();
        String beanName = "testBean";

        Object result = beanPostProcessor.postProcessAfterInitialization(nonProxyDataSource, beanName);

        assertSame(nonProxyDataSource, result);
    }

    @Test
    void postProcessAfterInitialization_WithNullBean_ShouldReturnNull() {
        String beanName = "testBean";

        Object result = beanPostProcessor.postProcessAfterInitialization(null, beanName);

        assertEquals(null, result);
    }

    @Test
    void
            postProcessAfterInitialization_WithNullBeanName_AndProxyDataSource_ShouldReturnLazyConnectionDataSourceProxy() {
        try (MockedStatic<ScopedProxyUtils> mockedScopedProxyUtils = mockStatic(ScopedProxyUtils.class)) {
            mockedScopedProxyUtils
                    .when(() -> ScopedProxyUtils.isScopedTarget(null))
                    .thenReturn(false);

            Object result = beanPostProcessor.postProcessAfterInitialization(mockProxyDataSource, null);

            assertNotNull(result);
            assertInstanceOf(LazyConnectionDataSourceProxy.class, result);
        }
    }
}
