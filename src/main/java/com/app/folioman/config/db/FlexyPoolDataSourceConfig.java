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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@AutoConfiguration(
        after = {DataSourceAutoConfiguration.class},
        before = {HibernateJpaAutoConfiguration.class})
@ConditionalOnClass({FlexyPoolDataSource.class, DataSource.class})
@ConditionalOnBean(DataSource.class)
public class FlexyPoolDataSourceConfig {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 2)
    static BeanPostProcessor flexyPoolDataSourceBeanPostProcessor(
            ObjectProvider<AppDataSourceProperties> appDataSourceProperties) {
        return new FlexyPoolDataSourceBeanPostProcessor(appDataSourceProperties);
    }

    private record FlexyPoolDataSourceBeanPostProcessor(ObjectProvider<AppDataSourceProperties> appDataSourceProperties)
            implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource dataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
                HikariDataSource hikariDataSource = getHikariDataSource(dataSource);
                if (hikariDataSource == null) {
                    // HikariDataSource is not available; return the original bean
                    return bean;
                }
                AppDataSourceProperties appDataSourceProperties = getAppDataSourceProperties();
                FlexyPoolConfiguration<HikariDataSource> flexyPoolConfiguration = new FlexyPoolConfiguration.Builder<>(
                                getClass().getSimpleName(), hikariDataSource, HikariCPPoolAdapter.FACTORY)
                        .setMetricsFactory(MetricsFactoryResolver.INSTANCE.resolve())
                        .setConnectionProxyFactory(ConnectionDecoratorFactoryResolver.INSTANCE.resolve())
                        .setMetricLogReporterMillis(TimeUnit.SECONDS.toMillis(5))
                        .setMetricNamingUniqueName(UniqueNamingStrategy.INSTANCE)
                        .setJmxEnabled(false)
                        .setJmxAutoStart(false)
                        .setConnectionAcquisitionTimeThresholdMillis(
                                appDataSourceProperties.getAcquisitionStrategy().getAcquisitionTimeout())
                        .setConnectionLeaseTimeThresholdMillis(
                                appDataSourceProperties.getAcquisitionStrategy().getLeaseTimeThreshold())
                        .setEventListenerResolver(() -> List.of(new ConnectionAcquisitionTimeoutEventListener()))
                        .build();

                return new FlexyPoolDataSource<>(
                        flexyPoolConfiguration,
                        new IncrementPoolOnTimeoutConnectionAcquisitionStrategy.Factory<>(
                                appDataSourceProperties.getMaxOvergrowPoolSize(),
                                appDataSourceProperties.getAcquisitionStrategy().getIncrementTimeout()),
                        new RetryConnectionAcquisitionStrategy.Factory<>(
                                appDataSourceProperties.getAcquisitionStrategy().getRetries()));
            }
            return bean;
        }

        private AppDataSourceProperties getAppDataSourceProperties() {
            return appDataSourceProperties.getIfAvailable();
        }

        private HikariDataSource getHikariDataSource(DataSource dataSource) {
            HikariDataSource hikariDataSource = null;
            if (dataSource instanceof HikariDataSource hikariDataSource1) {
                hikariDataSource = hikariDataSource1;
            }
            return hikariDataSource;
        }
    }
}
