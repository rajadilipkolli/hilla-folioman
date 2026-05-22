package com.app.folioman.pythonbridge.domain;

public class PythonTimeoutException extends PythonExecutionException {

    public PythonTimeoutException(String message, String command) {
        super(message, -1, null, command);
    }
}
