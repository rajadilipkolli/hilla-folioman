package com.app.folioman.config.db;

import com.vladmihalcea.flexypool.FlexyPoolDataSource;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.util.Map;
import javax.sql.DataSource;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbMetricsController {

    private final DataSource dataSource;

    DbMetricsController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/metrics/db/pool")
    Map<String, Object> poolMetrics() {
        Object current = dataSource;
        // Unwrap LazyConnectionDataSourceProxy -> getTargetDataSource()
        if (current instanceof LazyConnectionDataSourceProxy) {
            current = ((LazyConnectionDataSourceProxy) current).getTargetDataSource();
        }

        // If there's a ProxyDataSource (datasource-proxy), unwrap to its underlying datasource
        if (current instanceof ProxyDataSource) {
            current = ((ProxyDataSource) current).getDataSource();
        }

        // Unwrap FlexyPoolDataSource layers if present
        while (current instanceof FlexyPoolDataSource) {
            Object inner = ((FlexyPoolDataSource<?>) current).getTargetDataSource();
            // If getTargetDataSource() returns the same wrapper, break to avoid infinite loop
            if (inner == current) {
                break;
            }
            current = inner;
        }

        HikariDataSource hikariDataSource = null;
        if (current instanceof HikariDataSource) {
            hikariDataSource = (HikariDataSource) current;
        } else if (current instanceof FlexyPoolDataSource) {
            Object inner = ((FlexyPoolDataSource<?>) current).getTargetDataSource();
            if (inner instanceof HikariDataSource) {
                hikariDataSource = (HikariDataSource) inner;
            }
        }

        if (hikariDataSource == null) {
            throw new IllegalStateException("Could not locate HikariDataSource from configured DataSource wrappers");
        }
        HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
        if (hikariPoolMXBean == null) {
            throw new IllegalStateException("HikariCP pool has not been started yet");
        }
        return Map.of(
                "activeConnections", hikariPoolMXBean.getActiveConnections(),
                "idleConnections", hikariPoolMXBean.getIdleConnections(),
                "totalConnections", hikariPoolMXBean.getTotalConnections(),
                "threadsAwaitingConnection", hikariPoolMXBean.getThreadsAwaitingConnection());
    }
}
