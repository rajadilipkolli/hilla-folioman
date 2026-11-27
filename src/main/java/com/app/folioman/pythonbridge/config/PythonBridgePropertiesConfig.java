package com.app.folioman.pythonbridge.config;

import com.app.folioman.pythonbridge.PythonBridgeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PythonBridgeProperties.class)
public class PythonBridgePropertiesConfig {}
