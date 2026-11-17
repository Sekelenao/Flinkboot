package com.flinkboot.core.internal.resource;

import com.flinkboot.core.api.exception.resource.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClasspathResource")
class ClasspathResourceTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "com/flinkboot/core/internal/resource/ClasspathResourceTest.class",
        "/com/flinkboot/core/internal/resource/ClasspathResourceTest.class",
        "///com/flinkboot/core/internal/resource/ClasspathResourceTest.class"
    })
    @DisplayName("Should successfully load resource from classpath with various leading slash combinations")
    void shouldLoadResourceWithLeadingSlashes(String path) throws IOException {
        var resource = new ClasspathResource(path);
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

    @ParameterizedTest
    @ValueSource(strings = {"", "/", "///"})
    @DisplayName("Should throw ResourceNotFoundException when path resolves to empty")
    void shouldThrowExceptionWhenPathResolvesToEmpty(String path) {
        var resource = new ClasspathResource(path);
        assertThrows(ResourceNotFoundException.class, resource::inputStream);
    }

    @Test
    @DisplayName("Should fallback to ClasspathResource classloader when context classloader is null")
    void shouldFallbackToClassClassLoaderWhenContextClassLoaderIsNull() throws IOException {
        var originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(null);

            var path = "com/flinkboot/core/internal/resource/ClasspathResourceTest.class";
            var resource = new ClasspathResource(path);

            try (var is = resource.inputStream()) {
                assertNotNull(is);
                assertTrue(is.readAllBytes().length > 0);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
