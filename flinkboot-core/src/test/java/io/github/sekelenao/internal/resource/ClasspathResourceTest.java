package io.github.sekelenao.internal.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.sekelenao.api.exception.resource.ResourceNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ClasspathResource")
class ClasspathResourceTest {

    @Test
    @DisplayName("Should successfully load resource from classpath root")
    void shouldLoadResource() throws IOException {
        var resource = new ClasspathResource("test-resource.txt");
        try (var is = resource.inputStream()) {
            assertNotNull(is);
            var content = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            assertEquals("hello flinkboot", content);
        }
    }

    @Test
    @DisplayName("Should successfully load resource with absolute leading slash")
    void shouldLoadResourceWithLeadingSlash() throws IOException {
        var resource = new ClasspathResource("/test-resource.txt");
        try (var is = resource.inputStream()) {
            assertNotNull(is);
            var content = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            assertEquals("hello flinkboot", content);
        }
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when classpath resource is missing")
    void shouldThrowExceptionWhenNotFound() {
        var resource = new ClasspathResource("non-existent-resource.txt");
        assertThrows(ResourceNotFoundException.class, () -> resource.inputStream());
    }
}
