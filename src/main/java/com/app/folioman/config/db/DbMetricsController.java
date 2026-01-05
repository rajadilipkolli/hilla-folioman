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
        Object previous = null;
        label:
        while (current != previous && !(current instanceof HikariDataSource)) {
            previous = current;
            switch (current) {
                case LazyConnectionDataSourceProxy proxy:
                    current = proxy.getTargetDataSource();
                    break;
                case ProxyDataSource proxy:
                    current = proxy.getDataSource();
                    break;
                case FlexyPoolDataSource<?> flexyPool:
                    current = flexyPool.getTargetDataSource();
                    break;
                case null:
                default:
                    break label;
            }
        }

        HikariPoolMXBean hikariPoolMXBean = getHikariPoolMXBean(current);
        if (hikariPoolMXBean == null) {
            throw new IllegalStateException("HikariCP pool has not been started yet");
        }
        return Map.of(
                "activeConnections", hikariPoolMXBean.getActiveConnections(),
                "idleConnections", hikariPoolMXBean.getIdleConnections(),
                "totalConnections", hikariPoolMXBean.getTotalConnections(),
                "threadsAwaitingConnection", hikariPoolMXBean.getThreadsAwaitingConnection());
    }

    private static HikariPoolMXBean getHikariPoolMXBean(Object current) {
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
        return hikariDataSource.getHikariPoolMXBean();
    }
}
