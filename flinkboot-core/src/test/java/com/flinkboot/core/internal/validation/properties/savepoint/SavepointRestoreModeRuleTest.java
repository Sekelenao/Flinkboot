package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.RestoreMode;
import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SavepointRestoreModeRule")
class SavepointRestoreModeRuleTest {

    private final SavepointRestoreModeRule rule = new SavepointRestoreModeRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(RestoreMode.class)
    @DisplayName("Should fail when restore-mode is set without savepoint-path")
    void shouldFailWhenRestoreModeSetWithoutSavepointPath(RestoreMode restoreMode) {
        var props = new SavepointRestoreProperties(null, null, restoreMode, "/tmp/savepoints");

        assertEquals(
            Optional.of(new PropertyViolation(
                "restore-mode",
                "restore-mode cannot be set when savepoint-path is absent"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when only directory is specified")
    void shouldPassWhenOnlyDirectoryIsSpecified() {
        var props = new SavepointRestoreProperties(null, null, null, "/tmp/savepoints");

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when savepoint-path and restore-mode are specified with directory")
    void shouldPassWhenSavepointPathAndRestoreModeAreSpecifiedWithDirectory() {
        var props = new SavepointRestoreProperties("/tmp/savepoint", null, RestoreMode.CLAIM, "/tmp/savepoints");

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when savepoint-path and restore-mode are specified without directory")
    void shouldPassWhenSavepointPathAndRestoreModeAreSpecifiedWithoutDirectory() {
        var props = new SavepointRestoreProperties("/tmp/savepoint", null, RestoreMode.NO_CLAIM, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
