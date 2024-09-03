package com.example.application;

import com.example.application.common.RedisContainersConfig;
import com.example.application.common.SQLContainersConfig;
import org.springframework.boot.SpringApplication;

class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main)
                .with(SQLContainersConfig.class, RedisContainersConfig.class)
                .run(args);
    }
}
