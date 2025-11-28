package com.app.folioman;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.config.SQLContainersConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=validate"})
@Import(SQLContainersConfig.class)
@Disabled
class SchemaValidationPostgresTest {

    @Autowired
    DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void validateJpaMappingsWithDbSchema() {
        assertThat(dataSource).isNotNull().isInstanceOf(HikariDataSource.class);

        // Verify entity-to-table mappings
        Metamodel metamodel = entityManager.getMetamodel();
        Set<EntityType<?>> entities = metamodel.getEntities();

        for (EntityType<?> entity : entities) {
            String entityName = entity.getName();
            // Example assertion: Check if the table exists in the database
            // You can execute a query to check if the table exists
            boolean tableExists = !entityManager
                    .createQuery("SELECT count (1) FROM " + entityName)
                    .getResultList()
                    .isEmpty();
            assertThat(tableExists).isTrue();
        }
    }
}
