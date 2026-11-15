package com.flinkboot.core.internal.validation.properties.state;

import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.api.properties.state.StateBackendType;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateBackendIncrementalRule")
class StateBackendIncrementalRuleTest {

    private final StateBackendIncrementalRule rule = new StateBackendIncrementalRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when type is HASHMAP and incremental is true")
    void shouldFailWhenHashMapWithIncrementalTrue() {
        var props = new StateBackendProperties(StateBackendType.HASHMAP, true, null, null, null);

        assertEquals(
            Optional.of(new PropertyViolation(
                "incremental",
                "incremental cannot be true when state backend type is HASHMAP"
            )),
            rule.check(props)
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    @DisplayName("Should pass when type is HASHMAP and incremental is false or null")
    void shouldPassWhenHashMapWithIncrementalFalseOrNull(Boolean incremental) {
        var props = new StateBackendProperties(StateBackendType.HASHMAP, incremental, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when type is ROCKSDB and incremental is true")
    void shouldPassWhenRocksDbWithIncrementalTrue() {
        var props = new StateBackendProperties(StateBackendType.ROCKSDB, true, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when type is CUSTOM and incremental is true")
    void shouldPassWhenCustomWithIncrementalTrue() {
        var props = new StateBackendProperties(StateBackendType.CUSTOM, true, null, "com.example.MyBackend", null);

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when type is null and incremental is true")
    void shouldPassWhenTypeIsNullWithIncrementalTrue() {
        var props = new StateBackendProperties(null, true, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
