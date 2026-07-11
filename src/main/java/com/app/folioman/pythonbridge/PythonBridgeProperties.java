package com.app.folioman.pythonbridge;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.python")
public record PythonBridgeProperties(
        @DefaultValue("python3") String executable,
        String workingDirectory,
        @DefaultValue("60") int defaultTimeoutSeconds,
        Map<String, String> environment,
        @DefaultValue CasparserConfig casparser) {
    public record CasparserConfig(@DefaultValue("casparser") String executable) {}
}
