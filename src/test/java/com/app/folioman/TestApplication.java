package com.app.folioman;

import com.app.folioman.common.RedisContainersConfig;
import com.app.folioman.common.SQLContainersConfig;
import org.springframework.boot.SpringApplication;

class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main)
                .with(SQLContainersConfig.class, RedisContainersConfig.class)
                .run(args);
    }
}
