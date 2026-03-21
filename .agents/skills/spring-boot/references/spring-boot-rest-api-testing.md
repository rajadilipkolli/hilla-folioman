# Spring Boot REST API Testing

- [Key principles](#key-principles)
- [TestcontainersConfig](#testcontainersconfigjava)
- [BaseIT](#baseitjava)
- [Sample controller test](#sample-restcontroller-test)

## Key principles
Follow these principles when testing Spring Boot Web MVC REST APIs:

- Use `RestTestClient` to test API endpoints
- Use Testcontainers to setup test dependencies like databases, message brokers, etc
- Create a base test class for common setup and teardown
- Use `@SpringBootTest` with `webEnvironment = WebEnvironment.RANDOM_PORT` for integration testing
- Use `test` profile and create `application-test.properties` for test-specific configurations
- Use SQL scripts for database setup and teardown

## TestcontainersConfig.java
Add Testcontainers dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- if using PostgreSQL -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers-postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**IMPORTANT:** Make sure to use the Testcontainers 2.x maven dependency coordinates are used and non-deprecated classes are used.

- Use `org.testcontainers:testcontainers-junit-jupiter` instead of `org.testcontainers:junit-jupiter`
- Use `org.testcontainers:testcontainers-postgresql` instead of `org.testcontainers:postgresql`
- Use `org.testcontainers.postgresql.PostgreSQLContainer` instead of `org.testcontainers.containers.PostgreSQLContainer`


```java
package dev.sivalabs.projectname;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {\n\n    static GenericContainer<?> mailhog = new GenericContainer<>(\"mailhog/mailhog:v1.0.1\").withExposedPorts(1025);\n\n    static {\n        mailhog.start();\n    }\n\n    @Bean\n    @ServiceConnection\n    PostgreSQLContainer postgresContainer() {\n        return new PostgreSQLContainer(\"postgres:18-alpine\");\n    }\n\n    @Bean\n    @ServiceConnection(name = \"redis\")\n    GenericContainer<?> redisContainer() {\n        return new GenericContainer<>(DockerImageName.parse(\"redis:7-alpine\")).withExposedPorts(6379);\n    }\n\n    @Bean\n    DynamicPropertyRegistrar dynamicPropertyRegistrar() {\n        return (registry) -> {\n            registry.add(\"spring.mail.host\", mailhog::getHost);\n            registry.add(\"spring.mail.port\", mailhog::getFirstMappedPort);\n        };\n    }\n}\n```\n\n## BaseIT.java\n```java\npackage dev.sivalabs.projectname;\n\nimport static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;\n\nimport org.springframework.beans.factory.annotation.Autowired;\nimport org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;\nimport org.springframework.boot.test.context.SpringBootTest;\nimport org.springframework.context.annotation.Import;\nimport org.springframework.http.MediaType;\nimport org.springframework.test.context.jdbc.Sql;\nimport org.springframework.test.web.servlet.client.RestTestClient;\nimport tools.jackson.databind.json.JsonMapper;\n\n@SpringBootTest(webEnvironment = RANDOM_PORT)\n@Import(TestcontainersConfig.class)\n@AutoConfigureRestTestClient\n@Sql(\"/test-data.sql\")\npublic abstract class BaseIT {\n    public static final String ADMIN_EMAIL = \"admin@gmail.com\";\n    public static final String ADMIN_PASSWORD = \"Admin@1234\";\n    public static final String USER_EMAIL = \"siva@gmail.com\";\n    public static final String USER_PASSWORD = \"Siva@1234\";\n    \n    @Autowired\n    protected RestTestClient restTestClient;\n\n    @Autowired\n    protected JsonMapper jsonMapper;\n\n    protected String getAdminAuthToken() {\n        return getAuthToken(ADMIN_EMAIL, ADMIN_PASSWORD);\n    }\n\n    protected String getUserAuthToken() {\n        return getAuthToken(USER_EMAIL, USER_PASSWORD);\n    }\n    \n    protected String getAuthToken(String email, String password) {\n        //logic to generate JWT token\n        return \"jwt-token\";\n    }\n}\n```\n\n## Sample RestController Test\n```java\npackage dev.sivalabs.projectname.users.rest;\n\nimport static org.assertj.core.api.Assertions.assertThat;\n\nimport dev.sivalabs.projectname.BaseIT;\nimport dev.sivalabs.projectname.users.rest.dto.RegisterUserResponse;\nimport org.junit.jupiter.api.Test;\nimport org.junit.jupiter.params.ParameterizedTest;\nimport org.junit.jupiter.params.provider.CsvSource;\nimport org.springframework.http.MediaType;\nimport org.springframework.test.web.servlet.client.ExchangeResult;\n\nclass UserControllerTests extends BaseIT {\n\n    @Test\n    void shouldRegisterUserSuccessfully() {\n        RegisterUserResponse response = restTestClient\n                .post()\n                .uri(\"/api/users\")\n                .contentType(MediaType.APPLICATION_JSON)\n                .body(\"\"\"\n                        {\n                          \"fullName\":\"User123\",\n                          \"email\":\"user123@gmail.com\",\n                          \"password\":\"Secret@121212\"\n                        }\n                        \"\"\")\n                .exchange()\n                .expectStatus()\n                .isCreated()\n                .returnResult(RegisterUserResponse.class)\n                .getResponseBody();\n\n        assertThat(response).isNotNull();\n        assertThat(response.fullName()).isEqualTo(\"User123\");\n        assertThat(response.email()).isEqualTo(\"user123@gmail.com\");\n        assertThat(response.role().name()).isEqualTo(\"ROLE_USER\");\n    }\n\n    @ParameterizedTest\n    @CsvSource({\n        \",user1@gmail.com,password123,FullName\",\n        \"user1,,password123,Email\",\n        \"user1,user1@gmail.com,,Password\",\n    })\n    void shouldNotRegisterWithoutRequiredFields(String fullName, String email, String password, String errorFieldName) {\n\n        record ReqBody(String fullName, String email, String password) {}\n        \n        ExchangeResult exchangeResult = restTestClient\n                .post()\n                .uri(\"/api/users\")\n                .contentType(MediaType.APPLICATION_JSON)\n                .body(new ReqBody(fullName, email, password))\n                .exchange()\n                .expectStatus()\n                .isBadRequest()\n                .returnResult();\n\n        String responseJson = new String(exchangeResult.getResponseBodyContent());\n        assertThat(responseJson).contains(\"%s is required\".formatted(errorFieldName));\n    }\n\n    @Test\n    void shouldUpdateUserProfile() {\n        restTestClient\n                .put()\n                .uri(\"/api/users/me\")\n                .headers(h -> h.setBearerAuth(getUserAuthToken()))\n                .contentType(MediaType.APPLICATION_JSON)\n                .body(\"\"\"\n                        {\n                          \"fullName\": \"Siva Updated\"\n                        }\n                        \"\"\")\n                .exchange()\n                .expectStatus()\n                .isOk();\n    }\n\n    @Test\n    void shouldNotRegisterUserWithDuplicateEmail() {\n        restTestClient\n                .post()\n                .uri(\"/api/users\")\n                .contentType(MediaType.APPLICATION_JSON)\n                .body(\"\"\"\n                        {\n                          \"fullName\":\"New User\",\n                          \"email\":\"siva@gmail.com\",\n                          \"password\":\"Secret@121212\"\n                        }\n                        \"\"\")\n                .exchange()\n                .expectStatus()\n                .isBadRequest();\n    }\n\n    @Test\n    void shouldNotUpdateUserWithoutAuthentication() {\n        restTestClient\n                .put()\n                .uri(\"/api/users/me\")\n                .contentType(MediaType.APPLICATION_JSON)\n                .body(\"\"\"\n                        {\n                          \"fullName\": \"Updated Name\"\n                        }\n                        \"\"\")\n                .exchange()\n                .expectStatus()\n                .isUnauthorized();\n    }\n}\n```
