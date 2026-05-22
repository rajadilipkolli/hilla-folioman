package com.app.folioman.pythonbridge;

import com.app.folioman.pythonbridge.domain.PythonCommand;
import java.nio.file.Path;
import java.util.List;

public final class PythonCommands {

    private PythonCommands() {
        // utility class
    }

    public static PythonCommand script(Path scriptPath) {
        return new PythonCommand(null, scriptPath.toString(), List.of(), null, null, null);
    }

    public static PythonCommand module(String moduleName) {
        return new PythonCommand(null, "-m", List.of(moduleName), null, null, null);
    }

    public static PythonCommand command(String... args) {
        return new PythonCommand(null, null, List.of(args), null, null, null);
    }

    public static PythonCommand cli(String executable, String... args) {
        return new PythonCommand(executable, null, List.of(args), null, null, null);
    }
}
