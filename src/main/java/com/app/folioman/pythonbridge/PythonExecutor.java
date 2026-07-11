package com.app.folioman.pythonbridge;

public interface PythonExecutor {
    PythonResult execute(PythonCommand command);

    boolean isAvailable();
}
