package com.example.application;

import com.example.application.common.SQLContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest(classes = SQLContainersConfig.class)
class ApplicationTest {

    @Test
    void contextLoads() {
        ApplicationModules applicationModules = ApplicationModules.of(Application.class);

        applicationModules.verify();
    }

    @Test
    void createModulithsDocumentation() {

        new Documenter(ApplicationModules.of(Application.class)).writeDocumentation();
    }
}
