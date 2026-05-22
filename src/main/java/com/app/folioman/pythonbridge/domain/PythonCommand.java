package com.app.folioman.pythonbridge.domain;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record PythonCommand(
        @Nullable String customExecutable,
        @Nullable String script,
        List<String> arguments,
        @Nullable String workingDirectory,
        @Nullable Integer timeoutSeconds,
        @Nullable byte[] inputData) {
    public PythonCommand {
        arguments = List.copyOf(arguments);
    }

    public static PythonCommand command(String... args) {
        return new PythonCommand(null, null, List.of(args), null, null, null);
    }

    public static PythonCommand cli(String executable, String... args) {
        return new PythonCommand(executable, null, List.of(args), null, null, null);
    }

    public PythonCommand withArgument(String arg) {
        List<String> newArgs = new ArrayList<>(this.arguments);
        newArgs.add(arg);
        return new PythonCommand(
                this.customExecutable,
                this.script,
                newArgs,
                this.workingDirectory,
                this.timeoutSeconds,
                this.inputData);
    }

    public PythonCommand withArguments(List<String> args) {
        List<String> newArgs = new ArrayList<>(this.arguments);
        newArgs.addAll(args);
        return new PythonCommand(
                this.customExecutable,
                this.script,
                newArgs,
                this.workingDirectory,
                this.timeoutSeconds,
                this.inputData);
    }

    public PythonCommand withWorkingDirectory(String workingDirectory) {
        return new PythonCommand(
                this.customExecutable,
                this.script,
                this.arguments,
                workingDirectory,
                this.timeoutSeconds,
                this.inputData);
    }

    public PythonCommand withTimeoutSeconds(int timeoutSeconds) {
        return new PythonCommand(
                this.customExecutable,
                this.script,
                this.arguments,
                this.workingDirectory,
                timeoutSeconds,
                this.inputData);
    }

    public PythonCommand withInputData(byte[] inputData) {
        return new PythonCommand(
                this.customExecutable,
                this.script,
                this.arguments,
                this.workingDirectory,
                this.timeoutSeconds,
                inputData);
    }

    public PythonCommand withInputData(String inputData) {
        return withInputData(inputData.getBytes(StandardCharsets.UTF_8));
    }
}
