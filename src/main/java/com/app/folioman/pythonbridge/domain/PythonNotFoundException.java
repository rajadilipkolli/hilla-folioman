package com.app.folioman.pythonbridge.domain;

public class PythonNotFoundException extends PythonExecutionException {

    PythonNotFoundException(String message, String command) {
        super(message, -1, null, command);
    }

    PythonNotFoundException(String message, Throwable cause, String command) {
        super(message, cause, -1, null, command);
    }
}
