package com.app.folioman.config.db;

import com.vladmihalcea.flexypool.FlexyPoolDataSource;
import com.vladmihalcea.flexypool.adaptor.HikariCPPoolAdapter;
import com.vladmihalcea.flexypool.config.FlexyPoolConfiguration;
import com.vladmihalcea.flexypool.connection.ConnectionDecoratorFactoryResolver;
import com.vladmihalcea.flexypool.metric.MetricsFactoryResolver;
import com.vladmihalcea.flexypool.strategy.IncrementPoolOnTimeoutConnectionAcquisitionStrategy;
import com.vladmihalcea.flexypool.strategy.RetryConnectionAcquisitionStrategy;
import com.vladmihalcea.flexypool.strategy.UniqueNamingStrategy;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
        after = {DataSourceAutoConfiguration.class},
        before = {HibernateJpaAutoConfiguration.class})
@ConditionalOnClass({FlexyPoolDataSource.class, DataSource.class})
@ConditionalOnBean(DataSource.class)
public class FlexyPoolDataSourceConfig {

    private static int maxOverflowPoolSize = 5;
    private static int connectionAcquisitionThresholdMillis = 50;

    @Bean
    static FlexyPoolDataSourceBeanPostProcessor flexyPoolDataSourceBeanPostProcessor() {
        return new FlexyPoolDataSourceBeanPostProcessor(maxOverflowPoolSize, connectionAcquisitionThresholdMillis);
    }

    private record FlexyPoolDataSourceBeanPostProcessor(
            int maxOverflowPoolSize, int connectionAcquisitionThresholdMillis) implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource dataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
                FlexyPoolConfiguration<HikariDataSource> flexyPoolConfiguration = new FlexyPoolConfiguration.Builder<>(
                                getClass().getSimpleName(), (HikariDataSource) dataSource, HikariCPPoolAdapter.FACTORY)
                        .setMetricsFactory(MetricsFactoryResolver.INSTANCE.resolve())
                        .setConnectionProxyFactory(ConnectionDecoratorFactoryResolver.INSTANCE.resolve())
                        .setMetricLogReporterMillis(TimeUnit.SECONDS.toMillis(5))
                        .setMetricNamingUniqueName(UniqueNamingStrategy.INSTANCE)
                        .setJmxEnabled(false)
                        .setJmxAutoStart(false)
                        .setConnectionAcquisitionTimeThresholdMillis(50L)
                        .setConnectionLeaseTimeThresholdMillis(250L)
                        .setEventListenerResolver(() -> List.of(new ConnectionAcquisitionTimeoutEventListener()))
                        .build();

                return new FlexyPoolDataSource<>(
                        flexyPoolConfiguration,
                        new IncrementPoolOnTimeoutConnectionAcquisitionStrategy.Factory<>(
                                maxOverflowPoolSize, connectionAcquisitionThresholdMillis),
                        new RetryConnectionAcquisitionStrategy.Factory<>(2));
            }
            return bean;
        }
    }
}
