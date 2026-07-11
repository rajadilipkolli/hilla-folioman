package com.app.folioman.pythonbridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
class PythonExecutorImpl implements PythonExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonExecutorImpl.class);

    private final PythonBridgeProperties properties;
    private final JsonMapper jsonMapper;

    private final AtomicBoolean available = new AtomicBoolean(false);
    private final AtomicBoolean checked = new AtomicBoolean(false);

    PythonExecutorImpl(PythonBridgeProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean isAvailable() {
        if (checked.get()) {
            return available.get();
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(properties.executable(), "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            boolean isAvail = exitCode == 0;
            available.set(isAvail);
            checked.set(true);
            return isAvail;
        } catch (Exception e) {
            LOGGER.warn("Python executable not found or failed to run: {}", properties.executable(), e);
            available.set(false);
            checked.set(true);
            return false;
        }
    }

    public boolean isAvailable(@Nullable String executable) {
        if (executable == null || executable.equals(properties.executable())) {
            return isAvailable();
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(executable, "--version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            LOGGER.warn("Custom Python executable not found or failed to run: {}", executable, e);
            return false;
        }
    }

    @Override
    public PythonResult execute(PythonCommand command) {
        String executable = command.customExecutable() != null ? command.customExecutable() : properties.executable();
        if (!isAvailable(executable)) {
            throw new PythonNotFoundException(
                    "Python executable not available: " + executable, getCommandString(command));
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add(executable);
        if (command.script() != null) {
            cmdList.add(command.script());
        }
        cmdList.addAll(command.arguments());

        ProcessBuilder pb = new ProcessBuilder(cmdList);

        if (command.workingDirectory() != null) {
            pb.directory(new java.io.File(command.workingDirectory()));
        } else if (properties.workingDirectory() != null
                && !properties.workingDirectory().isBlank()) {
            pb.directory(new java.io.File(properties.workingDirectory()));
        }

        if (properties.environment() != null) {
            pb.environment().putAll(properties.environment());
        }

        pb.redirectErrorStream(true);

        long startTime = System.currentTimeMillis();
        Process process = null;
        try {
            process = pb.start();

            try (OutputStream os = process.getOutputStream()) {
                if (command.inputData() != null) {
                    os.write(command.inputData());
                    os.flush();
                }
            }

            int timeout =
                    command.timeoutSeconds() != null ? command.timeoutSeconds() : properties.defaultTimeoutSeconds();
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                process.waitFor();
                throw new PythonTimeoutException(
                        "Python process timed out after " + timeout + " seconds", getCommandString(command));
            }

            byte[] output;
            try (InputStream is = process.getInputStream()) {
                output = is.readAllBytes();
            }
            byte[] errorOutput;
            try (InputStream es = process.getErrorStream()) {
                errorOutput = es.readAllBytes();
            }

            int exitCode = process.exitValue();
            long executionTime = System.currentTimeMillis() - startTime;

            return new PythonResult(exitCode, output, errorOutput, executionTime, jsonMapper);

        } catch (IOException e) {
            throw new PythonExecutionException(
                    "Failed to start Python process", e, -1, null, getCommandString(command));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            throw new PythonExecutionException(
                    "Python process was interrupted", e, -1, null, getCommandString(command));
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private String getCommandString(PythonCommand command) {
        String exec = command.customExecutable() != null ? command.customExecutable() : properties.executable();
        return exec + " " + (command.script() != null ? command.script() + " " : "")
                + String.join(" ", command.arguments());
    }
}
