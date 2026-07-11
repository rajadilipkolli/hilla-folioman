package com.app.folioman.pythonbridge;

public class PythonTimeoutException extends PythonExecutionException {

    PythonTimeoutException(String message, String command) {
        super(message, -1, null, command);
    }
}
