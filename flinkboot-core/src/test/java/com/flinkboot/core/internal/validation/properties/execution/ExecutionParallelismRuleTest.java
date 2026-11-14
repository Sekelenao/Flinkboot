package com.flinkboot.core.internal.validation.properties.execution;

import com.flinkboot.core.api.properties.execution.ExecutionProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExecutionParallelismRule")
class ExecutionParallelismRuleTest {

    private final ExecutionParallelismRule rule = new ExecutionParallelismRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when parallelism exceeds max-parallelism")
    void shouldFailWhenParallelismExceedsMaxParallelism() {
        var props = new ExecutionProperties(null, 8, 4, null, null, null);

        assertEquals(
            Optional.of(new PropertyViolation(
                "parallelism",
                "parallelism (8) cannot exceed max-parallelism (4)"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when parallelism equals max-parallelism")
    void shouldPassWhenParallelismEqualsMaxParallelism() {
        var props = new ExecutionProperties(null, 4, 4, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when parallelism or max-parallelism is absent")
    void shouldPassWhenEitherIsAbsent() {
        var props = new ExecutionProperties(null, 4, null, null, null, null);

        assertTrue(rule.check(props).isEmpty());
    }
}
