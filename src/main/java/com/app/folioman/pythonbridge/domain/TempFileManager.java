package com.app.folioman.pythonbridge.domain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempFileManager implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileManager.class);

    TempFileManager() {
        // package-private constructor for internal domain support
    }

    private final List<Path> trackedFiles = new ArrayList<>();

    public Path createInputFile(String prefix, String suffix, byte[] content) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        trackedFiles.add(tempFile);
        if (content != null) {
            Files.write(tempFile, content);
        }
        return tempFile;
    }

    public Path createInputFile(String prefix, String suffix, String content) throws IOException {
        return createInputFile(prefix, suffix, content != null ? content.getBytes(StandardCharsets.UTF_8) : null);
    }

    public Path createOutputFile(String prefix, String suffix) throws IOException {
        Path tempFile = Files.createTempFile(prefix, suffix);
        trackedFiles.add(tempFile);
        return tempFile;
    }

    public byte[] readOutputFile(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public String readOutputFileAsText(Path path) throws IOException {
        return new String(readOutputFile(path), StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        for (Path file : trackedFiles) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete temporary file: {}", file, e);
            }
        }
        trackedFiles.clear();
    }
}
