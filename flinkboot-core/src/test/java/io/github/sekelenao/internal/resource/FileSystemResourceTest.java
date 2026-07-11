package io.github.sekelenao.internal.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sekelenao.api.exception.resource.ResourceAccessException;
import io.github.sekelenao.api.exception.resource.ResourceNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("FileSystemResource")
class FileSystemResourceTest {

    @Test
    @DisplayName("Should successfully load file from filesystem")
    void shouldLoadFile(@TempDir Path tempDir) throws IOException {
        var tempFile = tempDir.resolve("local-file.yaml");
        Files.writeString(tempFile, "filesystem config content");

        var resource = new FileSystemResource(tempFile.toAbsolutePath().toString());
        try (var is = resource.inputStream()) {
            assertNotNull(is);
            var content = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            assertEquals("filesystem config content", content);
        }
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when file does not exist")
    void shouldThrowExceptionWhenNotFound() {
        var resource = new FileSystemResource("/non/existent/path/file.yaml");
        assertThrows(ResourceNotFoundException.class, resource::inputStream);
    }

    @Test
    @DisplayName("Should throw ResourceAccessException inheriting from FlinkbootException when location is a directory")
    void shouldThrowResourceAccessExceptionWhenIsDirectory(@TempDir Path tempDir) {
        var resource = new FileSystemResource(tempDir.toAbsolutePath().toString());
        var exception = assertThrows(ResourceAccessException.class, resource::inputStream);
        org.junit.jupiter.api.Assertions.assertInstanceOf(io.github.sekelenao.api.exception.FlinkbootException.class, exception);
    }

}
