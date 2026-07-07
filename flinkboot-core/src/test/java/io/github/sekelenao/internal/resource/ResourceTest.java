package io.github.sekelenao.internal.resource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sekelenao.api.exception.resource.UnrecognizedResourceException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Resource Factory")
class ResourceTest {

    @Test
    @DisplayName("Should throw NullPointerException when location is null")
    void shouldThrowExceptionWhenLocationIsNull() {
        assertThrows(NullPointerException.class, () -> Resource.get(null));
    }

    @Test
    @DisplayName("Should throw UnrecognizedResourceException when location is empty")
    void shouldThrowExceptionWhenLocationIsEmpty() {
        assertThrows(UnrecognizedResourceException.class, () -> Resource.get(""));
    }

    @Test
    @DisplayName("Should return ClasspathResource when prefixed with classpath: or resource:")
    void shouldReturnClasspathResourceWithPrefixes() {
        assertAll(
            () -> assertInstanceOf(ClasspathResource.class, Resource.get("classpath:config.yaml")),
            () -> assertInstanceOf(ClasspathResource.class, Resource.get("resource:config.yaml"))
        );
    }

    @Test
    @DisplayName("Should return FileSystemResource when prefixed with file:")
    void shouldReturnFileSystemResourceWithPrefix() {
        assertInstanceOf(FileSystemResource.class, Resource.get("file:/path/to/config.yaml"));
    }

    @Test
    @DisplayName("Should throw UnrecognizedResourceException when no prefix is specified")
    void shouldThrowExceptionWhenNoPrefix() {
        assertAll(
            () -> assertThrows(UnrecognizedResourceException.class, () -> Resource.get("config.yaml")),
            () -> assertThrows(UnrecognizedResourceException.class, () -> Resource.get("/path/to/config.yaml"))
        );
    }

    @Test
    @DisplayName("Should throw UnrecognizedResourceException when prefix is unsupported")
    void shouldThrowExceptionForUnsupportedPrefix() {
        assertThrows(UnrecognizedResourceException.class, () -> Resource.get("http://localhost/config.yaml"));
    }
}
