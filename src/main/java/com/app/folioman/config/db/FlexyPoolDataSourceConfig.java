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
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(FlexyPoolDataSourceConfig.class);

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 2)
    static BeanPostProcessor flexyPoolDataSourceBeanPostProcessor(
            ObjectProvider<AppDataSourceProperties> appDataSourceProperties) {
        return new FlexyPoolDataSourceBeanPostProcessor(appDataSourceProperties);
    }

    static record FlexyPoolDataSourceBeanPostProcessor(ObjectProvider<AppDataSourceProperties> appDataSourceProperties)
            implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource dataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
                HikariDataSource hikariDataSource = getHikariDataSource(dataSource);
                if (hikariDataSource == null) {
                    // HikariDataSource is not available; return the original bean
                    return bean;
                }

                // Apply Hikari optimizations
                optimizeHikariPool(hikariDataSource, beanName);

                AppDataSourceProperties appDataSourceProperties = getAppDataSourceProperties();

                // Configure Flexy Pool with improved metrics and leak detection
                FlexyPoolConfiguration<HikariDataSource> flexyPoolConfiguration =
                        buildFlexyPoolConfiguration(hikariDataSource, appDataSourceProperties, beanName);

                // Create and return the FlexyPoolDataSource with improved strategies
                return createFlexyPoolDataSource(flexyPoolConfiguration, appDataSourceProperties);
            }
            return bean;
        }

        FlexyPoolConfiguration<HikariDataSource> buildFlexyPoolConfiguration(
                HikariDataSource hikariDataSource, AppDataSourceProperties appDataSourceProperties, String beanName) {

            return new FlexyPoolConfiguration.Builder<>(beanName, hikariDataSource, HikariCPPoolAdapter.FACTORY)
                    .setMetricsFactory(MetricsFactoryResolver.INSTANCE.resolve())
                    .setConnectionProxyFactory(ConnectionDecoratorFactoryResolver.INSTANCE.resolve())
                    .setMetricLogReporterMillis(
                            appDataSourceProperties.getMetrics().getReportingIntervalMs())
                    .setMetricNamingUniqueName(UniqueNamingStrategy.INSTANCE)
                    .setJmxEnabled(appDataSourceProperties.getMetrics().isDetailed())
                    .setJmxAutoStart(appDataSourceProperties.getMetrics().isDetailed())
                    .setConnectionAcquisitionTimeThresholdMillis(
                            appDataSourceProperties.getAcquisitionStrategy().getAcquisitionTimeout())
                    .setConnectionLeaseTimeThresholdMillis(
                            appDataSourceProperties.getAcquisitionStrategy().getLeaseTimeThreshold())
                    .setEventListenerResolver(() -> List.of(new ConnectionAcquisitionTimeoutEventListener()))
                    .build();
        }

        FlexyPoolDataSource<HikariDataSource> createFlexyPoolDataSource(
                FlexyPoolConfiguration<HikariDataSource> flexyPoolConfiguration,
                AppDataSourceProperties appDataSourceProperties) {

            return new FlexyPoolDataSource<>(
                    flexyPoolConfiguration,
                    new IncrementPoolOnTimeoutConnectionAcquisitionStrategy.Factory<>(
                            appDataSourceProperties.getMaxOvergrowPoolSize(),
                            appDataSourceProperties.getAcquisitionStrategy().getIncrementTimeout()),
                    new RetryConnectionAcquisitionStrategy.Factory<>(
                            appDataSourceProperties.getAcquisitionStrategy().getRetries()));
        }

        /**
         * Apply optimizations to HikariCP connection pool
         */
        void optimizeHikariPool(HikariDataSource hikariDataSource, String poolName) {
            AppDataSourceProperties props = getAppDataSourceProperties();

            // Enable leak detection if configured
            if (props.getConnectionLeak().isEnabled()) {
                hikariDataSource.setLeakDetectionThreshold(
                        props.getConnectionLeak().getThresholdMs());
                log.info(
                        "Enabled connection leak detection for pool '{}' with threshold: {} ms",
                        poolName,
                        props.getConnectionLeak().getThresholdMs());
            }

            // Set other optimal settings if not already configured
            if (hikariDataSource.getMinimumIdle() == hikariDataSource.getMaximumPoolSize()) {
                // For better performance under steady load, minimum idle connections should be lower
                hikariDataSource.setMinimumIdle(Math.max(2, hikariDataSource.getMaximumPoolSize() / 4));
                log.info(
                        "Optimized minimum idle connections for pool '{}': {}",
                        poolName,
                        hikariDataSource.getMinimumIdle());
            }

            // Use faster connection test query if database allows it
            hikariDataSource.setConnectionTestQuery(null); // Let JDBC4 driver handle validation
        }

        AppDataSourceProperties getAppDataSourceProperties() {
            return appDataSourceProperties.getIfAvailable();
        }

        HikariDataSource getHikariDataSource(DataSource dataSource) {
            HikariDataSource hikariDataSource = null;
            if (dataSource instanceof HikariDataSource hikariDataSource1) {
                hikariDataSource = hikariDataSource1;
            }
            return hikariDataSource;
        }
    }
}
