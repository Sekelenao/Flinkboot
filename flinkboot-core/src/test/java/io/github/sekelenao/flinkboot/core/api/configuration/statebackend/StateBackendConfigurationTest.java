package io.github.sekelenao.flinkboot.core.api.configuration.statebackend;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateBackendConfiguration")
class StateBackendConfigurationTest {

    private static final YAMLMapper mapper = YAMLMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .findAndAddModules()
        .build();

    private static final Validator validator;
    static {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should successfully deserialize from valid YAML with all fields")
        void shouldDeserializeValidYaml() throws Exception {
            var yaml = "type: rocksdb\n" +
                "checkpoint-storage: file:///tmp/checkpoints\n" +
                "incremental: true\n";

            var config = mapper.readValue(yaml, StateBackendConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(FlinkStateBackendType.ROCKSDB, config.type()),
                () -> assertTrue(config.checkpointStorage().isPresent()),
                () -> assertEquals("file:///tmp/checkpoints", config.checkpointStorage().get()),
                () -> assertTrue(config.incremental().isPresent()),
                () -> assertEquals(true, config.incremental().get())
            );
        }

        @Test
        @DisplayName("Should deserialize successfully without optional properties")
        void shouldDeserializeWithoutOptionalProperties() throws Exception {
            var yaml = "type: hashmap\n";

            var config = mapper.readValue(yaml, StateBackendConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(FlinkStateBackendType.HASHMAP, config.type()),
                () -> assertTrue(config.checkpointStorage().isEmpty()),
                () -> assertTrue(config.incremental().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid properties")
        void shouldPassValidation() {
            var config = new StateBackendConfiguration(
                FlinkStateBackendType.CHANGELOG,
                "file:///tmp/checkpoints",
                true
            );

            Set<ConstraintViolation<StateBackendConfiguration>> violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }

        @Test
        @DisplayName("Should throw NullPointerException when type is null")
        void shouldThrowWhenTypeIsNull() {
            org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class,
                () -> new StateBackendConfiguration(null, "file:///tmp/checkpoints", true)
            );
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("Should return expected values from getters when all parameters are present")
        void testGettersWithAllParameters() {
            var config = new StateBackendConfiguration(
                FlinkStateBackendType.ROCKSDB,
                "file:///tmp/checkpoints",
                false
            );

            assertAll(
                () -> assertEquals(FlinkStateBackendType.ROCKSDB, config.type()),
                () -> assertTrue(config.checkpointStorage().isPresent()),
                () -> assertEquals("file:///tmp/checkpoints", config.checkpointStorage().get()),
                () -> assertTrue(config.incremental().isPresent()),
                () -> assertEquals(false, config.incremental().get())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Equals and HashCode should work correctly")
        void testEqualsAndHashCode() {
            var config1 = new StateBackendConfiguration(
                FlinkStateBackendType.ROCKSDB,
                "file:///tmp/checkpoints",
                true
            );
            var config2 = new StateBackendConfiguration(
                FlinkStateBackendType.ROCKSDB,
                "file:///tmp/checkpoints",
                true
            );
            var configDiffType = new StateBackendConfiguration(
                FlinkStateBackendType.HASHMAP,
                "file:///tmp/checkpoints",
                true
            );

            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, null),
                () -> assertNotEquals(config1, "string-object"),
                () -> assertNotEquals(config1, configDiffType),
                () -> assertNotNull(config1.toString())
            );
        }
    }
}
