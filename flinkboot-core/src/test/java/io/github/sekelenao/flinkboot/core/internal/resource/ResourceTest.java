package io.github.sekelenao.flinkboot.core.internal.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.UnrecognizedResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    @DisplayName("Should successfully resolve resource case-insensitively")
    void shouldResolveResourceCaseInsensitively() {
        assertAll(
            () -> assertInstanceOf(ClasspathResource.class, Resource.get("Classpath:config.yaml")),
            () -> assertInstanceOf(ClasspathResource.class, Resource.get("RESOURCE:config.yaml")),
            () -> assertInstanceOf(FileSystemResource.class, Resource.get("File:config.yaml"))
        );
    }
}
