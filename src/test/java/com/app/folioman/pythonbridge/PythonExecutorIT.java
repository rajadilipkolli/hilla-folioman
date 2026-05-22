package com.app.folioman.pythonbridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.app.folioman.pythonbridge.domain.PythonExecutionException;
import com.app.folioman.pythonbridge.domain.PythonResult;
import com.app.folioman.shared.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PythonExecutorIT extends AbstractIntegrationTest {

    @Autowired
    private PythonExecutor pythonExecutor;

    @Test
    void verifyPythonIsAvailable() {
        assertThat(pythonExecutor.isAvailable()).isTrue();
    }

    @Test
    void executePythonVersion() {
        PythonResult result = pythonExecutor.execute(PythonCommands.command("--version"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.asText()).contains("Python");
    }

    @Test
    void executeInlineScriptWithJson() {
        String script = "import json; print(json.dumps({'status': 'ok', 'value': 42}))";
        PythonResult result = pythonExecutor.execute(PythonCommands.command("-c", script));
        assertThat(result.isSuccess()).isTrue();

        Map<String, Object> json = result.asJson(Map.class);
        assertThat(json).containsEntry("status", "ok");
        assertThat(json).containsEntry("value", 42);
    }

    @Test
    void executeFailingScript() {
        PythonResult result = pythonExecutor.execute(PythonCommands.command("-c", "import sys; sys.exit(1)"));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.exitCode()).isEqualTo(1);

        assertThatExceptionOfType(PythonExecutionException.class)
                .isThrownBy(() -> result.orThrow())
                .withMessageContaining("exit code 1");
    }
}
