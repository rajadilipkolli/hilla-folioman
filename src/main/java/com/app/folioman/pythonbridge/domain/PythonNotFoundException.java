package com.app.folioman.pythonbridge.domain;

public class PythonNotFoundException extends PythonExecutionException {

    public PythonNotFoundException(String message, String command) {
        super(message, -1, null, command);
    }

    public PythonNotFoundException(String message, Throwable cause, String command) {
        super(message, cause, -1, null, command);
    }
}
