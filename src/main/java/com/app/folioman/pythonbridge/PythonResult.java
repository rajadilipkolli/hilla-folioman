package com.app.folioman.pythonbridge;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import tools.jackson.databind.json.JsonMapper;

public record PythonResult(
        int exitCode, byte[] stdout, byte[] stderr, long executionTimeMillis, JsonMapper jsonMapper) {

    public PythonResult {
        stdout = stdout == null ? new byte[0] : Arrays.copyOf(stdout, stdout.length);
        stderr = stderr == null ? new byte[0] : Arrays.copyOf(stderr, stderr.length);
    }

    @Override
    public byte[] stdout() {
        return Arrays.copyOf(stdout, stdout.length);
    }

    @Override
    public byte[] stderr() {
        return Arrays.copyOf(stderr, stderr.length);
    }

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
