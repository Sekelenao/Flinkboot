package io.github.sekelenao.flinkboot.core.api.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.UnrecognizedResourceException;
import io.github.sekelenao.flinkboot.core.internal.resource.ClasspathResource;
import io.github.sekelenao.flinkboot.core.internal.resource.FileSystemResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Resource")
class ResourceTest {

    @Nested
    @DisplayName("Of")
    class Of {

        @Test
        @DisplayName("Should throw NullPointerException when location is null")
        void shouldThrowExceptionWhenLocationIsNull() {
            assertThrows(NullPointerException.class, () -> Resource.of(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should throw UnrecognizedResourceException when location is empty or blank")
        void shouldThrowExceptionWhenLocationIsEmptyOrBlank(String location) {
            assertThrows(UnrecognizedResourceException.class, () -> Resource.of(location));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "file:",
            "file:   ",
            "classpath:",
            "classpath:   ",
            "resource:",
            "resource:   "
        })
        @DisplayName("Should throw UnrecognizedResourceException when location suffix is empty or blank")
        void shouldThrowExceptionWhenSuffixIsEmptyOrBlank(String location) {
            assertThrows(UnrecognizedResourceException.class, () -> Resource.of(location));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "classpath:config.yaml",
            "resource:config.yaml",
            "Classpath:config.yaml",
            "RESOURCE:config.yaml"
        })
        @DisplayName("Should return ClasspathResource when prefixed with classpath: or resource:")
        void shouldReturnClasspathResourceWithPrefixes(String location) {
            assertInstanceOf(ClasspathResource.class, Resource.of(location));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "file:/path/to/config.yaml",
            "File:/path/to/config.yaml",
            "FILE:config.yaml"
        })
        @DisplayName("Should return FileSystemResource when prefixed with file:")
        void shouldReturnFileSystemResourceWithPrefix(String location) {
            assertInstanceOf(FileSystemResource.class, Resource.of(location));
        }

        @ParameterizedTest
        @ValueSource(strings = {"config.yaml", "/path/to/config.yaml"})
        @DisplayName("Should throw UnrecognizedResourceException when no prefix is specified")
        void shouldThrowExceptionWhenNoPrefix(String location) {
            assertThrows(UnrecognizedResourceException.class, () -> Resource.of(location));
        }

        @Test
        @DisplayName("Should throw UnrecognizedResourceException when prefix is unsupported")
        void shouldThrowExceptionForUnsupportedPrefix() {
            assertThrows(UnrecognizedResourceException.class, () -> Resource.of("http://localhost/config.yaml"));
        }
    }

    @Nested
    @DisplayName("Statelessness")
    class Statelessness {

        @Test
        @DisplayName("Should produce independent InputStreams for classpath resource upon consecutive calls")
        void shouldProduceIndependentStreamsForClasspathResource() throws IOException {
            var resource = Resource.of("classpath:io/github/sekelenao/flinkboot/core/internal/resource/ClasspathResourceTest.class");
            byte[] firstBytes;
            byte[] secondBytes;

            try (var firstStream = resource.inputStream()) {
                firstBytes = firstStream.readAllBytes();
            }

            try (var secondStream = resource.inputStream()) {
                secondBytes = secondStream.readAllBytes();
            }

            assertAll(
                () -> assertTrue(firstBytes.length > 0),
                () -> assertEquals(firstBytes.length, secondBytes.length)
            );
        }

        @Test
        @DisplayName("Should produce independent InputStreams for filesystem resource upon consecutive calls")
        void shouldProduceIndependentStreamsForFileSystemResource(@TempDir Path tempDir) throws IOException {
            var tempFile = tempDir.resolve("test-file.txt");
            Files.writeString(tempFile, "stateless test content", StandardCharsets.UTF_8);

            var resource = Resource.of("file:" + tempFile.toAbsolutePath());
            String firstContent;
            String secondContent;

            try (var firstStream = resource.inputStream()) {
                firstContent = new String(firstStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            try (var secondStream = resource.inputStream()) {
                secondContent = new String(secondStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            assertAll(
                () -> assertEquals("stateless test content", firstContent),
                () -> assertEquals("stateless test content", secondContent)
            );
        }
    }
}
