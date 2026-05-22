package com.app.folioman.pythonbridge.domain;

import com.app.folioman.pythonbridge.PythonExecutor;
import com.app.folioman.pythonbridge.config.PythonBridgeProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    @Override
    public PythonResult execute(PythonCommand command) {
        if (!isAvailable()) {
            throw new PythonNotFoundException(
                    "Python executable not available: " + properties.executable(), getCommandString(command));
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add(command.customExecutable() != null ? command.customExecutable() : properties.executable());
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

            if (command.inputData() != null) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(command.inputData());
                    os.flush();
                }
            }

            byte[] output;
            try (InputStream is = process.getInputStream()) {
                output = is.readAllBytes();
            }

            int timeout =
                    command.timeoutSeconds() != null ? command.timeoutSeconds() : properties.defaultTimeoutSeconds();
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                throw new PythonTimeoutException(
                        "Python process timed out after " + timeout + " seconds", getCommandString(command));
            }

            int exitCode = process.exitValue();
            long executionTime = System.currentTimeMillis() - startTime;

            return new PythonResult(exitCode, output, new byte[0], executionTime, jsonMapper);

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
