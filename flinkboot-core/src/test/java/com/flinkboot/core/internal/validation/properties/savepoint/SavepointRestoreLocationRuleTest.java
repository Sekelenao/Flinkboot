package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SavepointRestoreLocationRule")
class SavepointRestoreLocationRuleTest {

    private final SavepointRestoreLocationRule rule = new SavepointRestoreLocationRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when both savepoint-path and directory are absent")
    void shouldFailWhenBothAreAbsent() {
        var props = new SavepointRestoreProperties(null, null, null, null);

        assertEquals(
            Optional.of(new PropertyViolation(
                "savepoint-path",
                "at least one of savepoint-path or directory must be specified"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when savepoint-path is specified")
    void shouldPassWhenSavepointPathIsSpecified() {
        var props = new SavepointRestoreProperties("s3://bucket/savepoint", null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when directory is specified")
    void shouldPassWhenDirectoryIsSpecified() {
        var props = new SavepointRestoreProperties(null, null, null, "s3://bucket/dir");

        assertTrue(rule.check(props).isEmpty());
    }
}
