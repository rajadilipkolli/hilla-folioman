package com.app.folioman.pythonbridge.domain;

import org.jspecify.annotations.Nullable;

public class PythonExecutionException extends RuntimeException {

    private final int exitCode;

    @Nullable
    private final String stderr;

    private final String command;

    public PythonExecutionException(String message, int exitCode, @Nullable String stderr, String command) {
        super(message);
        this.exitCode = exitCode;
        this.stderr = stderr;
        this.command = command;
    }

    public PythonExecutionException(
            String message, Throwable cause, int exitCode, @Nullable String stderr, String command) {
        super(message, cause);
        this.exitCode = exitCode;
        this.stderr = stderr;
        this.command = command;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Nullable
    public String getStderr() {
        return stderr;
    }

    public String getCommand() {
        return command;
    }
}
