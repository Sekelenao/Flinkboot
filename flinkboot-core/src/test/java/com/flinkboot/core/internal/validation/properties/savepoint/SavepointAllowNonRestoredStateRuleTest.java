package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SavepointAllowNonRestoredStateRule")
class SavepointAllowNonRestoredStateRuleTest {

    private final SavepointAllowNonRestoredStateRule rule = new SavepointAllowNonRestoredStateRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should fail when allow-non-restored-state is set without savepoint-path")
    void shouldFailWhenAllowNonRestoredStateSetWithoutSavepointPath(boolean allowNonRestoredState) {
        var props = new SavepointRestoreProperties(null, allowNonRestoredState, null, "/tmp/savepoints");

        assertEquals(
            Optional.of(new PropertyViolation(
                "allow-non-restored-state",
                "allow-non-restored-state cannot be set when savepoint-path is absent"
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
    @DisplayName("Should pass when savepoint-path and allow-non-restored-state are specified with directory")
    void shouldPassWhenSavepointPathAndOptionAreSpecifiedWithDirectory() {
        var props = new SavepointRestoreProperties("/tmp/savepoint", true, null, "/tmp/savepoints");

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when savepoint-path and allow-non-restored-state are specified without directory")
    void shouldPassWhenSavepointPathAndOptionAreSpecifiedWithoutDirectory() {
        var props = new SavepointRestoreProperties("/tmp/savepoint", false, null, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
