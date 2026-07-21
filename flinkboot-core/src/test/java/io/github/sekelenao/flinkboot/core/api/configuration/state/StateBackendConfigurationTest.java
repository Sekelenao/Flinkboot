package io.github.sekelenao.flinkboot.core.api.configuration.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidStateBackendConfigurationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateBackendConfiguration Tests")
class StateBackendConfigurationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return empty optionals when all properties are null")
        void shouldReturnEmptyOptionals() {
            var config = new StateBackendConfiguration(null, null, null, null, null, null);

            assertAll(
                () -> assertTrue(config.type().isEmpty()),
                () -> assertTrue(config.checkpointStorage().isEmpty()),
                () -> assertTrue(config.storagePath().isEmpty()),
                () -> assertTrue(config.incremental().isEmpty()),
                () -> assertTrue(config.latencyTracking().isEmpty()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation & Exception Tests")
    class ValidationAndExceptionTests {

        @Test
        @DisplayName("Should pass validation with valid HASHMAP configuration")
        void shouldPassWithValidHashMap() {
            var config = new StateBackendConfiguration(
                StateBackendType.HASHMAP,
                CheckpointStorageType.JOBMANAGER,
                null,
                false,
                true,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertTrue(violations.isEmpty()),
                () -> assertEquals(StateBackendType.HASHMAP, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.JOBMANAGER, config.checkpointStorage().orElseThrow()),
                () -> assertTrue(config.latencyTracking().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should pass validation with valid CUSTOM configuration")
        void shouldPassWithValidCustomBackend() {
            var config = new StateBackendConfiguration(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                "s3://bucket/checkpoints",
                true,
                false,
                "com.example.MyCustomStateBackend"
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertTrue(violations.isEmpty()),
                () -> assertEquals(StateBackendType.CUSTOM, config.type().orElseThrow()),
                () -> assertEquals("com.example.MyCustomStateBackend", config.customClass().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should throw Exception when CUSTOM state backend type is specified without custom-class")
        void shouldThrowExceptionWhenCustomClassMissingForCustomType() {
            assertThrows(
                InvalidStateBackendConfigurationException.class,
                () -> new StateBackendConfiguration(StateBackendType.CUSTOM, null, null, null, null, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when custom-class is specified for non-CUSTOM state backend type")
        void shouldThrowExceptionWhenCustomClassProvidedForNonCustomType() {
            assertThrows(
                InvalidStateBackendConfigurationException.class,
                () -> new StateBackendConfiguration(StateBackendType.ROCKSDB, null, null, null, null, "com.example.MyCustomStateBackend")
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should respect equals and hashCode contract")
        void shouldRespectEqualsAndHashCode() {
            var config1 = new StateBackendConfiguration(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, "s3://bucket", true, false, null);
            var config2 = new StateBackendConfiguration(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, "s3://bucket", true, false, null);
            var config3 = new StateBackendConfiguration(StateBackendType.HASHMAP, CheckpointStorageType.JOBMANAGER, null, false, false, null);

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, config3)
            );
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize StateBackendConfiguration from JSON correctly")
        void shouldDeserializeFromJson() throws Exception {
            String json = "{\n" +
                "  \"type\": \"ROCKSDB\",\n" +
                "  \"checkpoint-storage\": \"FILESYSTEM\",\n" +
                "  \"storage-path\": \"s3://my-bucket/checkpoints\",\n" +
                "  \"incremental\": true,\n" +
                "  \"latency-tracking\": true\n" +
                "}";

            StateBackendConfiguration config = mapper.readValue(json, StateBackendConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(StateBackendType.ROCKSDB, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.checkpointStorage().orElseThrow()),
                () -> assertEquals("s3://my-bucket/checkpoints", config.storagePath().orElseThrow()),
                () -> assertTrue(config.incremental().orElseThrow()),
                () -> assertTrue(config.latencyTracking().orElseThrow())
            );
        }
    }
}
