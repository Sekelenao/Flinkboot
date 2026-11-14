package com.flinkboot.core.internal.validation.properties.state;

import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.api.properties.state.StateBackendType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateBackendCustomClassRule")
class StateBackendCustomClassRuleTest {

    private final StateBackendCustomClassRule rule = new StateBackendCustomClassRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when type is CUSTOM and custom-class is absent")
    void shouldFailWhenCustomWithoutClass() {
        var props = new StateBackendProperties(StateBackendType.CUSTOM, null, null, null, null);

        assertEquals(
            Optional.of(new PropertyViolation(
                "custom-class",
                "custom-class must be specified when state backend type is CUSTOM"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when type is not CUSTOM and custom-class is specified")
    void shouldFailWhenNonCustomWithClass() {
        var props = new StateBackendProperties(StateBackendType.ROCKSDB, null, null, "com.example.MyBackend", null);

        assertEquals(
            Optional.of(new PropertyViolation(
                "custom-class",
                "custom-class can only be specified when state backend type is CUSTOM"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when type is CUSTOM and custom-class is specified")
    void shouldPassWhenCustomWithClass() {
        var props = new StateBackendProperties(StateBackendType.CUSTOM, null, null, "com.example.MyBackend", null);

        assertTrue(rule.check(props).isEmpty());
    }
}
