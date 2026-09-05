package io.github.sekelenao.flinkboot.test.internal;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PojoValidationTask")
class PojoValidationTaskTest {

    @Test
    @DisplayName("Should create instance with valid non-null parameters")
    void shouldCreateWithValidParameters() {
        var typeInfo = TypeInformation.of(String.class);
        assertDoesNotThrow(() -> new PojoValidationTask<>("some/path", typeInfo));
    }

    @Test
    @DisplayName("Should throw NullPointerException when path is null")
    void shouldThrowWhenPathIsNull() {
        var typeInfo = TypeInformation.of(String.class);

        var ex = assertThrows(NullPointerException.class, () -> new PojoValidationTask<>(null, typeInfo));
        assertEquals("path must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when typeInfo is null")
    void shouldThrowWhenTypeInfoIsNull() {
        var ex = assertThrows(NullPointerException.class, () -> new PojoValidationTask<>("some/path", null));
        assertEquals("typeInfo must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should return correct values from accessors")
    void shouldReturnCorrectAccessorValues() {
        var typeInfo = TypeInformation.of(String.class);
        var task = new PojoValidationTask<>("some/path", typeInfo);
        assertEquals("some/path", task.path());
        assertEquals(typeInfo, task.typeInfo());
    }
}