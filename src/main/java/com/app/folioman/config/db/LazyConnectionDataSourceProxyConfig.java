package com.app.folioman.config.db;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.observation.boot.autoconfigure.DataSourceObservationAutoConfiguration;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

/**
 * Configuration class that sets up a lazy-loading data source proxy to improve performance by
 * deferring physical database connections until they are actually needed. This optimization is
 * particularly useful in scenarios where database operations don't always occur during request
 * processing.
 */
@AutoConfiguration(
        after = {DataSourceObservationAutoConfiguration.class},
        before = {HibernateJpaAutoConfiguration.class})
@ConditionalOnClass(ProxyDataSource.class)
class LazyConnectionDataSourceProxyConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    static BeanPostProcessor lazyConnectionDataSourceProxyBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof ProxyDataSource proxyDataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
                    return new LazyConnectionDataSourceProxy(proxyDataSource);
                }
                return bean;
            }
        };
    }
}
