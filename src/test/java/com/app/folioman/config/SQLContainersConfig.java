package com.app.folioman.config;

import java.time.Duration;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Import({AuditConfiguration.class})
public class SQLContainersConfig {

    private static final Network network = Network.newNetwork();

    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(
                    DockerImageName.parse("postgres").withTag("19.0-alpine"))
            .withCommand("postgres", "-c", "max_connections=200")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withReuse(true);

    private static final GenericContainer<?> mfNavDataContainer;

    static {
        postgreSQLContainer.start();

        Testcontainers.exposeHostPorts(postgreSQLContainer.getFirstMappedPort());
        String jdbcUrl = postgreSQLContainer.getJdbcUrl().replace("localhost", "host.testcontainers.internal");

        mfNavDataContainer = new GenericContainer<>(
                        DockerImageName.parse(
                                "ghcr.io/rajadilipkolli/historical-mf-nav-data@sha256:6761892e2a3ec131dc90030c85b894a4e95893e120401f9112db8676bcccebde"))
                .withNetwork(network)
                .withExposedPorts(8080)
                .withEnv("SPRING_PROFILES_ACTIVE", "docker")
                .withEnv("JAVA_TOOL_OPTIONS", "-Dspring.profiles.active=docker")
                .withEnv("SPRING_DATASOURCE_URL", jdbcUrl)
                .withEnv("SPRING_DATASOURCE_USERNAME", postgreSQLContainer.getUsername())
                .withEnv("SPRING_DATASOURCE_PASSWORD", postgreSQLContainer.getPassword())
                .withEnv("DAILY_NAV_DATABASE_TYPE", "postgres")
                .withEnv("DAILY_NAV_URL", jdbcUrl)
                .withEnv("DAILY_NAV_USERNAME", postgreSQLContainer.getUsername())
                .withEnv("DAILY_NAV_PASSWORD", postgreSQLContainer.getPassword())
                .withEnv("DAILY_NAV_AUTO_INIT", "true")
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("mfNavDataContainer")))
                .waitingFor(Wait.forLogMessage(".*Started DailyNavApplication.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .dependsOn(postgreSQLContainer);

        mfNavDataContainer.start();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSQLContainer() {
        return postgreSQLContainer;
    }

    @Bean
    DynamicPropertyRegistrar dynamicProperties() {
        return registry -> registry.add("app.nav.mfapi.data-url", SQLContainersConfig::mfNavDataUrl);
    }

    public static String mfNavDataUrl() {
        return "http://" + mfNavDataContainer.getHost() + ":" + mfNavDataContainer.getMappedPort(8080) + "/api/v1";
    }
}
