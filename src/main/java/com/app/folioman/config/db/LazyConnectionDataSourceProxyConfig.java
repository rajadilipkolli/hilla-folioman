package com.app.folioman.config.db;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.observation.boot.autoconfigure.DataSourceObservationAutoConfiguration;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
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
class LazyConnectionDataSourceProxyConfig implements BeanFactoryPostProcessor {

    @Bean
    static LazyConnectionDataSourceProxyBeanPostProcessor lazyConnectionDataSourceProxy() {
        return new LazyConnectionDataSourceProxyBeanPostProcessor();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Update existing dataSource bean definition
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("dataSource");
        beanDefinition.getPropertyValues().add("poolName", "jobrunr");
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        listableBeanFactory.registerBeanDefinition("jobrunrDataSource", beanDefinition);
    }

    private static class LazyConnectionDataSourceProxyBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ProxyDataSource proxyDataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
                return new LazyConnectionDataSourceProxy(proxyDataSource);
            }
            return bean;
        }
    }
}
