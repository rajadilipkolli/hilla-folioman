package com.app.folioman;

import com.app.folioman.config.NoSQLContainersConfig;
import com.app.folioman.config.SQLContainersConfig;
import org.springframework.boot.SpringApplication;

class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main)
                .with(SQLContainersConfig.class, NoSQLContainersConfig.class)
                .run(args);
    }
}
