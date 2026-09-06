package io.github.sekelenao.flinkboot.core.api.properties.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidStateBackendPropertiesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StateBackendProperties Tests")
class StateBackendPropertiesTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should instantiate with valid arguments")
        void shouldInstantiateWithValidArguments() {
            var config = new StateBackendProperties(
                StateBackendType.HASHMAP,
                CheckpointStorageType.JOBMANAGER,
                false,
                true,
                null
            );
            assertNotNull(config);
        }

        @Test
        @DisplayName("Should instantiate with null arguments")
        void shouldInstantiateWithNullArguments() {
            var config = new StateBackendProperties(null, null, null, null, null);
            assertNotNull(config);
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        @DisplayName("Should return expected values when fields are provided (asymmetric booleans)")
        void shouldReturnExpectedValuesWhenFieldsAreProvided() {
            var config = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                null
            );

            assertAll(
                () -> assertTrue(config.type().isPresent()),
                () -> assertEquals(StateBackendType.ROCKSDB, config.type().get()),
                () -> assertTrue(config.checkpointStorage().isPresent()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.checkpointStorage().get()),
                () -> assertTrue(config.incremental().isPresent()),
                () -> assertTrue(config.incremental().get()),
                () -> assertTrue(config.latencyTracking().isPresent()),
                () -> assertFalse(config.latencyTracking().get()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return expected values for custom state backend with reverse asymmetric booleans")
        void shouldReturnExpectedValuesForCustomStateBackend() {
            var config = new StateBackendProperties(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                false,
                true,
                "com.example.MyCustomStateBackend"
            );

            assertAll(
                () -> assertTrue(config.type().isPresent()),
                () -> assertEquals(StateBackendType.CUSTOM, config.type().get()),
                () -> assertTrue(config.checkpointStorage().isPresent()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.checkpointStorage().get()),
                () -> assertTrue(config.incremental().isPresent()),
                () -> assertFalse(config.incremental().get()),
                () -> assertTrue(config.latencyTracking().isPresent()),
                () -> assertTrue(config.latencyTracking().get()),
                () -> assertTrue(config.customClass().isPresent()),
                () -> assertEquals("com.example.MyCustomStateBackend", config.customClass().get())
            );
        }

        @Test
        @DisplayName("Should return empty optionals when all properties are null")
        void shouldReturnEmptyOptionals() {
            var config = new StateBackendProperties(null, null, null, null, null);

            assertAll(
                () -> assertTrue(config.type().isEmpty()),
                () -> assertTrue(config.checkpointStorage().isEmpty()),
                () -> assertTrue(config.incremental().isEmpty()),
                () -> assertTrue(config.latencyTracking().isEmpty()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass validation with valid HASHMAP configuration")
        void shouldPassWithValidHashMap() {
            var config = new StateBackendProperties(
                StateBackendType.HASHMAP,
                CheckpointStorageType.JOBMANAGER,
                false,
                true,
                null
            );

            assertAll(
                () -> assertEquals(StateBackendType.HASHMAP, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.JOBMANAGER, config.checkpointStorage().orElseThrow()),
                () -> assertFalse(config.incremental().orElseThrow()),
                () -> assertTrue(config.latencyTracking().orElseThrow()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }

        @Test
        @DisplayName("Should pass validation with valid CUSTOM configuration")
        void shouldPassWithValidCustomBackend() {
            var config = new StateBackendProperties(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                "com.example.MyCustomStateBackend"
            );

            assertAll(
                () -> assertEquals(StateBackendType.CUSTOM, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.checkpointStorage().orElseThrow()),
                () -> assertTrue(config.incremental().orElseThrow()),
                () -> assertFalse(config.latencyTracking().orElseThrow()),
                () -> assertEquals("com.example.MyCustomStateBackend", config.customClass().orElseThrow())
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Should throw exception when custom-class is null, empty, or blank for CUSTOM state backend")
        void shouldThrowExceptionWhenCustomClassNullOrBlankForCustomType(String customClass) {
            var exception = assertThrows(
                InvalidStateBackendPropertiesException.class,
                () -> new StateBackendProperties(StateBackendType.CUSTOM, null, null, null, customClass)
            );
            assertEquals(
                "custom-class must be specified when state backend type is CUSTOM",
                exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(value = StateBackendType.class, names = "CUSTOM", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should throw exception when custom-class is provided for non-CUSTOM state backend")
        void shouldThrowExceptionWhenCustomClassProvidedForNonCustomType(StateBackendType type) {
            var exception = assertThrows(
                InvalidStateBackendPropertiesException.class,
                () -> new StateBackendProperties(type, null, null, null, "com.example.MyCustomStateBackend")
            );
            assertEquals(
                "custom-class can only be specified when state backend type is CUSTOM",
                exception.getMessage()
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("Should permit null or blank custom-class for non-CUSTOM state backend")
        void shouldPermitNullOrBlankCustomClassForNonCustomType(String customClass) {
            var config = new StateBackendProperties(
                StateBackendType.HASHMAP,
                CheckpointStorageType.JOBMANAGER,
                false,
                true,
                customClass
            );

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(StateBackendType.HASHMAP, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.JOBMANAGER, config.checkpointStorage().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should satisfy equals and hashCode contract")
        void shouldSatisfyEqualsAndHashCodeContract() {
            var config1 = new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, true, false, null);
            var config2 = new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, true, false, null);

            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config2, config1),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, null),
                () -> assertNotEquals(config1, "differentType")
            );
        }

        @Test
        @DisplayName("Should not equal when individual fields differ")
        void shouldNotEqualWhenIndividualFieldsDiffer() {
            var base = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                null
            );
            var diffType = new StateBackendProperties(
                StateBackendType.HASHMAP,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                null
            );
            var diffStorage = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.JOBMANAGER,
                true,
                false,
                null
            );
            var diffIncremental = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                false,
                false,
                null
            );
            var diffLatency = new StateBackendProperties(
                StateBackendType.ROCKSDB,
                CheckpointStorageType.FILESYSTEM,
                true,
                true,
                null
            );

            var custom1 = new StateBackendProperties(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                "com.example.CustomBackend1"
            );
            var custom2 = new StateBackendProperties(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                "com.example.CustomBackend2"
            );

            assertAll(
                () -> assertNotEquals(base, diffType),
                () -> assertNotEquals(base, diffStorage),
                () -> assertNotEquals(base, diffIncremental),
                () -> assertNotEquals(base, diffLatency),
                () -> assertNotEquals(base, custom1),
                () -> assertNotEquals(custom1, custom2)
            );
        }

        @Test
        @DisplayName("Should return meaningful string representation")
        void shouldReturnMeaningfulStringRepresentation() {
            var config = new StateBackendProperties(
                StateBackendType.CUSTOM,
                CheckpointStorageType.FILESYSTEM,
                true,
                false,
                "com.example.CustomFactory"
            );
            var str = config.toString();
            assertAll(
                () -> assertTrue(str.contains("StateBackendProperties")),
                () -> assertTrue(str.contains("type=CUSTOM")),
                () -> assertTrue(str.contains("checkpointStorage=FILESYSTEM")),
                () -> assertTrue(str.contains("incremental=true")),
                () -> assertTrue(str.contains("latencyTracking=false")),
                () -> assertTrue(str.contains("customClass='com.example.CustomFactory'"))
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should deserialize ROCKSDB state backend with asymmetric booleans from JSON")
        void shouldDeserializeRocksDbFromJson() throws Exception {
            var json = "{\n" +
                "  \"type\": \"ROCKSDB\",\n" +
                "  \"checkpoint-storage\": \"FILESYSTEM\",\n" +
                "  \"incremental\": true,\n" +
                "  \"latency-tracking\": false\n" +
                "}";

            var config = mapper.readValue(json, StateBackendProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(StateBackendType.ROCKSDB, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.checkpointStorage().orElseThrow()),
                () -> assertTrue(config.incremental().orElseThrow()),
                () -> assertFalse(config.latencyTracking().orElseThrow()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize CUSTOM state backend with custom-class from JSON")
        void shouldDeserializeCustomBackendFromJson() throws Exception {
            var json = "{\n" +
                "  \"type\": \"CUSTOM\",\n" +
                "  \"checkpoint-storage\": \"FILESYSTEM\",\n" +
                "  \"incremental\": false,\n" +
                "  \"latency-tracking\": true,\n" +
                "  \"custom-class\": \"com.example.MyCustomStateBackend\"\n" +
                "}";

            var config = mapper.readValue(json, StateBackendProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(StateBackendType.CUSTOM, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.checkpointStorage().orElseThrow()),
                () -> assertFalse(config.incremental().orElseThrow()),
                () -> assertTrue(config.latencyTracking().orElseThrow()),
                () -> assertEquals("com.example.MyCustomStateBackend", config.customClass().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize CHANGELOG state backend with JOBMANAGER storage from JSON")
        void shouldDeserializeChangelogWithJobManagerStorageFromJson() throws Exception {
            var json = "{\n" +
                "  \"type\": \"CHANGELOG\",\n" +
                "  \"checkpoint-storage\": \"JOBMANAGER\"\n" +
                "}";

            var config = mapper.readValue(json, StateBackendProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(StateBackendType.CHANGELOG, config.type().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.JOBMANAGER, config.checkpointStorage().orElseThrow()),
                () -> assertTrue(config.incremental().isEmpty()),
                () -> assertTrue(config.latencyTracking().isEmpty()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize empty JSON object with all empty optionals")
        void shouldDeserializeEmptyJsonObject() throws Exception {
            var config = mapper.readValue("{}", StateBackendProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertTrue(config.type().isEmpty()),
                () -> assertTrue(config.checkpointStorage().isEmpty()),
                () -> assertTrue(config.incremental().isEmpty()),
                () -> assertTrue(config.latencyTracking().isEmpty()),
                () -> assertTrue(config.customClass().isEmpty())
            );
        }
    }
}
