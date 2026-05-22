package com.app.folioman.pythonbridge;

import com.app.folioman.pythonbridge.domain.PythonCommand;
import com.app.folioman.pythonbridge.domain.PythonResult;

public interface PythonExecutor {
    PythonResult execute(PythonCommand command);

    boolean isAvailable();
}
