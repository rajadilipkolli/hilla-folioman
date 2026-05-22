package com.app.folioman.pythonbridge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.app.folioman.pythonbridge.config.PythonBridgeProperties;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class PythonExecutorImplTest {

    @Mock
    private PythonBridgeProperties properties;

    private PythonExecutorImpl executor;

    @BeforeEach
    void setUp() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        executor = new PythonExecutorImpl(properties, jsonMapper);
    }

    @Test
    void executeSuccess() {
        when(properties.executable()).thenReturn("python3");
        when(properties.defaultTimeoutSeconds()).thenReturn(10);

        PythonCommand command = PythonCommand.command("-c", "print('hello')");
        PythonResult result = executor.execute(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.exitCode()).isEqualTo(0);
        assertThat(result.asText().trim()).isEqualTo("hello");
    }

    @Test
    void executeWithTimeout() {
        when(properties.executable()).thenReturn("python3");
        // Windows 'ping' or Python sleep to simulate timeout
        PythonCommand command =
                PythonCommand.command("-c", "import time; time.sleep(5)").withTimeoutSeconds(1);

        assertThatExceptionOfType(PythonTimeoutException.class)
                .isThrownBy(() -> executor.execute(command))
                .withMessageContaining("timed out after 1 seconds");
    }

    @Test
    void executeScriptNotFound() {
        when(properties.executable()).thenReturn("python3");
        when(properties.defaultTimeoutSeconds()).thenReturn(10);

        PythonCommand command = PythonCommand.command("nonexistent_script.py");
        PythonResult result = executor.execute(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.exitCode()).isNotEqualTo(0);
        assertThat(result.asText()).contains("nonexistent_script.py");

        assertThatExceptionOfType(PythonExecutionException.class).isThrownBy(() -> result.orThrow());
    }

    @Test
    void executeWithInputDataAndJson() {
        when(properties.executable()).thenReturn("python3");
        when(properties.defaultTimeoutSeconds()).thenReturn(10);

        String pyScript = "import sys, json\ndata = sys.stdin.read()\nprint(json.dumps({'received': data.strip()}))";
        PythonCommand command = PythonCommand.command("-c", pyScript).withInputData("test-input");

        PythonResult result = executor.execute(command);

        assertThat(result.isSuccess()).isTrue();

        Map<String, String> parsed = result.asJson(Map.class);
        assertThat(parsed).containsEntry("received", "test-input");
    }
}
