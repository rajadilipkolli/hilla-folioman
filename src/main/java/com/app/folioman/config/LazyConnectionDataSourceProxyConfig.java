package com.app.folioman.config;

import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.lang.NonNull;

/**
 * Configuration class that sets up a lazy-loading data source proxy to improve performance by
 * deferring physical database connections until they are actually needed. This optimization is
 * particularly useful in scenarios where database operations don't always occur during request
 * processing.
 */
@Configuration(proxyBeanMethods = false)
class LazyConnectionDataSourceProxyConfig implements BeanFactoryPostProcessor {

    /**
     * Processes the bean factory after its standard initialization.
     * This method:
     * 1. Registers a new LazyConnectionDataSourceProxy as the primary datasource
     * 2. Configures the original datasource for JobRunr usage
     *
     * @throws BeansException if the "dataSource" bean is not found
     */
    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;

        if (!listableBeanFactory.containsBean("dataSource")) {
            throw new BeansException("Required 'dataSource' bean is not defined") {};
        }
        // Register LazyConnectionDataSourceProxy as a primary bean
        listableBeanFactory.registerBeanDefinition(
                "lazyConnectionDataSourceProxy", createLazyConnectionDataSourceProxyDefinition(listableBeanFactory));

        // Update existing dataSource bean definition
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("dataSource");
        beanDefinition.getPropertyValues().add("poolName", "jobrunr");
        listableBeanFactory.registerBeanDefinition("jobrunrDataSource", beanDefinition);
    }

    private GenericBeanDefinition createLazyConnectionDataSourceProxyDefinition(
            DefaultListableBeanFactory listableBeanFactory) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(LazyConnectionDataSourceProxy.class);
        beanDefinition.setInstanceSupplier(
                () -> new LazyConnectionDataSourceProxy(listableBeanFactory.getBean("dataSource", DataSource.class)));
        beanDefinition.setPrimary(true); // Set this bean as primary
        return beanDefinition;
    }
}
