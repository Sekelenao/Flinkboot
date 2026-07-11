package io.github.sekelenao.internal.resource;

import io.github.sekelenao.api.exception.resource.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClasspathResource")
class ClasspathResourceTest {

    @Test
    @DisplayName("Should successfully load resource from classpath root")
    void shouldLoadResource() throws IOException {
        var resource = new ClasspathResource("io/github/sekelenao/internal/resource/ClasspathResourceTest.class");
        try (var is = resource.inputStream()) {
            assertNotNull(is);
            assertTrue(is.readAllBytes().length > 0);
        }
    }

    @Test
    @DisplayName("Should successfully load resource with absolute leading slash")
    void shouldLoadResourceWithLeadingSlash() throws IOException {
        var resource = new ClasspathResource("/io/github/sekelenao/internal/resource/ClasspathResourceTest.class");
        try (var is = resource.inputStream()) {
            assertNotNull(is);
            assertTrue(is.readAllBytes().length > 0);
        }
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when classpath resource is missing")
    void shouldThrowExceptionWhenNotFound() {
        var resource = new ClasspathResource("non-existent-resource.txt");
        assertThrows(ResourceNotFoundException.class, resource::inputStream);
    }
}
