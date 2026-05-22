package com.app.folioman.pythonbridge.domain;

import java.nio.charset.StandardCharsets;
import tools.jackson.databind.json.JsonMapper;

public record PythonResult(
        int exitCode, byte[] stdout, byte[] stderr, long executionTimeMillis, JsonMapper jsonMapper) {
    public boolean isSuccess() {
        return exitCode == 0;
    }

    public String asText() {
        return new String(stdout, StandardCharsets.UTF_8);
    }

    public <T> T asJson(Class<T> type) {
        try {
            return jsonMapper.readValue(stdout, type);
        } catch (Exception e) {
            String errorDetails = stderr != null && stderr.length > 0
                    ? new String(stderr, StandardCharsets.UTF_8)
                    : new String(stdout, StandardCharsets.UTF_8); // Fallback to stdout if stderr is merged
            throw new PythonExecutionException("Failed to parse JSON output", e, exitCode, errorDetails, "unknown");
        }
    }

    public PythonResult orThrow() {
        if (!isSuccess()) {
            String errorDetails = stderr != null && stderr.length > 0
                    ? new String(stderr, StandardCharsets.UTF_8)
                    : new String(stdout, StandardCharsets.UTF_8);
            throw new PythonExecutionException(
                    "Python process failed with exit code " + exitCode, exitCode, errorDetails, "unknown command");
        }
        return this;
    }
}
