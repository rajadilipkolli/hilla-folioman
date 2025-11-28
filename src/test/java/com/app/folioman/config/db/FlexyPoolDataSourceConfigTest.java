package com.app.folioman.config.db;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.vladmihalcea.flexypool.FlexyPoolDataSource;
import com.vladmihalcea.flexypool.config.FlexyPoolConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FlexyPoolDataSourceConfigTest {

    @Mock
    private ObjectProvider<AppDataSourceProperties> mockObjectProvider;

    @Mock
    private AppDataSourceProperties mockAppDataSourceProperties;

    @Mock
    private AppDataSourceProperties.Metrics mockMetrics;

    @Mock
    private AppDataSourceProperties.AcquisitionStrategy mockAcquisitionStrategy;

    @Mock
    private AppDataSourceProperties.ConnectionLeak mockConnectionLeak;

    @Mock
    private HikariDataSource mockHikariDataSource;

    @Mock
    private DataSource mockDataSource;

    private FlexyPoolDataSourceConfig.FlexyPoolDataSourceBeanPostProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new FlexyPoolDataSourceConfig.FlexyPoolDataSourceBeanPostProcessor(mockObjectProvider);

        when(mockObjectProvider.getIfAvailable()).thenReturn(mockAppDataSourceProperties);
        when(mockAppDataSourceProperties.getMetrics()).thenReturn(mockMetrics);
        when(mockAppDataSourceProperties.getAcquisitionStrategy()).thenReturn(mockAcquisitionStrategy);
        when(mockAppDataSourceProperties.getConnectionLeak()).thenReturn(mockConnectionLeak);

        when(mockMetrics.getReportingIntervalMs()).thenReturn(30000L);
        when(mockMetrics.isDetailed()).thenReturn(true);
        when(mockAcquisitionStrategy.getAcquisitionTimeout()).thenReturn(5000L);
        when(mockAcquisitionStrategy.getLeaseTimeThreshold()).thenReturn(10000L);
        when(mockAcquisitionStrategy.getIncrementTimeout()).thenReturn(2000);
        when(mockAcquisitionStrategy.getRetries()).thenReturn(3);
        when(mockAppDataSourceProperties.getMaxOvergrowPoolSize()).thenReturn(5);
        when(mockConnectionLeak.isEnabled()).thenReturn(true);
        when(mockConnectionLeak.getThresholdMs()).thenReturn(60000L);
    }

    @Test
    void flexyPoolDataSourceBeanPostProcessor_shouldReturnBeanPostProcessor() {
        BeanPostProcessor result = FlexyPoolDataSourceConfig.flexyPoolDataSourceBeanPostProcessor(mockObjectProvider);

        assertNotNull(result);
        assertInstanceOf(FlexyPoolDataSourceConfig.FlexyPoolDataSourceBeanPostProcessor.class, result);
    }

    @Test
    void postProcessAfterInitialization_withHikariDataSource_shouldReturnFlexyPoolDataSource() {
        // Set minimumIdle equal to maximumPoolSize so the processor optimizes minimum idle
        when(mockHikariDataSource.getMinimumIdle()).thenReturn(5);
        when(mockHikariDataSource.getMaximumPoolSize()).thenReturn(5);

        Object result = processor.postProcessAfterInitialization(mockHikariDataSource, "testBean");

        assertNotNull(result);
        assertInstanceOf(FlexyPoolDataSource.class, result);
        verify(mockHikariDataSource).setLeakDetectionThreshold(60000L);
        verify(mockHikariDataSource).setMinimumIdle(2);
        verify(mockHikariDataSource).setConnectionTestQuery(null);
    }

    @Test
    void postProcessAfterInitialization_withNonHikariDataSource_shouldReturnOriginalBean() {
        Object result = processor.postProcessAfterInitialization(mockDataSource, "testBean");

        assertSame(mockDataSource, result);
    }

    @Test
    void postProcessAfterInitialization_withScopedTarget_shouldReturnOriginalBean() {
        String scopedBeanName = "scopedTarget.testBean";

        Object result = processor.postProcessAfterInitialization(mockHikariDataSource, scopedBeanName);

        assertSame(mockHikariDataSource, result);
    }

    @Test
    void postProcessAfterInitialization_withNonDataSource_shouldReturnOriginalBean() {
        String nonDataSource = "notADataSource";

        Object result = processor.postProcessAfterInitialization(nonDataSource, "testBean");

        assertSame(nonDataSource, result);
    }

    @Test
    void getHikariDataSource_withHikariDataSource_shouldReturnHikariDataSource() {
        HikariDataSource result = processor.getHikariDataSource(mockHikariDataSource);

        assertSame(mockHikariDataSource, result);
    }

    @Test
    void getHikariDataSource_withNonHikariDataSource_shouldReturnNull() {
        HikariDataSource result = processor.getHikariDataSource(mockDataSource);

        assertNull(result);
    }

    @Test
    void optimizeHikariPool_withLeakDetectionEnabled_shouldConfigureLeakDetection() {
        when(mockHikariDataSource.getMinimumIdle()).thenReturn(5);
        when(mockHikariDataSource.getMaximumPoolSize()).thenReturn(5);

        processor.optimizeHikariPool(mockHikariDataSource, "testPool");

        verify(mockHikariDataSource).setLeakDetectionThreshold(60000L);
        verify(mockHikariDataSource).setConnectionTestQuery(null);
    }

    @Test
    void optimizeHikariPool_withLeakDetectionDisabled_shouldNotConfigureLeakDetection() {
        when(mockConnectionLeak.isEnabled()).thenReturn(false);
        when(mockHikariDataSource.getMinimumIdle()).thenReturn(3);
        when(mockHikariDataSource.getMaximumPoolSize()).thenReturn(10);

        processor.optimizeHikariPool(mockHikariDataSource, "testPool");

        verify(mockHikariDataSource, never()).setLeakDetectionThreshold(anyLong());
        verify(mockHikariDataSource).setConnectionTestQuery(null);
    }

    @Test
    void optimizeHikariPool_withMinimumIdleEqualsMaximumPool_shouldOptimizeMinimumIdle() {
        when(mockHikariDataSource.getMinimumIdle()).thenReturn(10);
        when(mockHikariDataSource.getMaximumPoolSize()).thenReturn(10);

        processor.optimizeHikariPool(mockHikariDataSource, "testPool");

        verify(mockHikariDataSource).setMinimumIdle(2);
    }

    @Test
    void optimizeHikariPool_withMinimumIdleDifferentFromMaximumPool_shouldNotOptimizeMinimumIdle() {
        when(mockHikariDataSource.getMinimumIdle()).thenReturn(5);
        when(mockHikariDataSource.getMaximumPoolSize()).thenReturn(10);

        processor.optimizeHikariPool(mockHikariDataSource, "testPool");

        verify(mockHikariDataSource, never()).setMinimumIdle(anyInt());
    }

    @Test
    void optimizeHikariPool_withSmallMaximumPool_shouldSetMinimumIdleToAtLeastTwo() {
        when(mockHikariDataSource.getMinimumIdle()).thenReturn(4);
        when(mockHikariDataSource.getMaximumPoolSize()).thenReturn(4);

        processor.optimizeHikariPool(mockHikariDataSource, "testPool");

        verify(mockHikariDataSource).setMinimumIdle(2);
    }

    @Test
    void getAppDataSourceProperties_shouldReturnPropertiesFromProvider() {
        AppDataSourceProperties result = processor.getAppDataSourceProperties();

        assertSame(mockAppDataSourceProperties, result);
        verify(mockObjectProvider).getIfAvailable();
    }

    @Test
    void getAppDataSourceProperties_withNullProvider_shouldReturnNull() {
        when(mockObjectProvider.getIfAvailable()).thenReturn(null);

        AppDataSourceProperties result = processor.getAppDataSourceProperties();

        assertNull(result);
    }

    @Test
    void buildFlexyPoolConfiguration_shouldCreateConfigurationWithCorrectSettings() {
        FlexyPoolConfiguration<HikariDataSource> result =
                processor.buildFlexyPoolConfiguration(mockHikariDataSource, mockAppDataSourceProperties, "testBean");

        assertNotNull(result);
        verify(mockMetrics).getReportingIntervalMs();
        verify(mockMetrics, times(2)).isDetailed();
        verify(mockAcquisitionStrategy).getAcquisitionTimeout();
        verify(mockAcquisitionStrategy).getLeaseTimeThreshold();
    }

    @Test
    void createFlexyPoolDataSource_shouldCreateFlexyPoolDataSource() {
        // Build a real FlexyPoolConfiguration to avoid NPEs from a mocked config internal state
        FlexyPoolConfiguration<HikariDataSource> realConfig =
                processor.buildFlexyPoolConfiguration(mockHikariDataSource, mockAppDataSourceProperties, "testBean");

        FlexyPoolDataSource<HikariDataSource> result =
                processor.createFlexyPoolDataSource(realConfig, mockAppDataSourceProperties);

        assertNotNull(result);
    }
}
