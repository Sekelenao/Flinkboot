package com.flinkboot.core.api.properties.savepoint;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SavepointRestoreProperties Tests")
class SavepointRestorePropertiesTest {

    private static final YAMLMapper mapper = YAMLMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .findAndAddModules()
        .build();

    private static final Validator validator;

    static {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize valid YAML with all fields")
        void shouldDeserializeValidYamlWithAllFields() throws Exception {
            var yaml = "savepoint-path: /tmp/savepoint-1\n" +
                "allow-non-restored-state: true\n" +
                "restore-mode: CLAIM\n" +
                "directory: /tmp/savepoints\n";

            var config = mapper.readValue(yaml, SavepointRestoreProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("/tmp/savepoint-1", config.savepointPath().orElseThrow()),
                () -> assertTrue(config.allowNonRestoredState().isPresent()),
                () -> assertTrue(config.allowNonRestoredState().get()),
                () -> assertTrue(config.restoreMode().isPresent()),
                () -> assertEquals(RestoreMode.CLAIM, config.restoreMode().get()),
                () -> assertEquals("/tmp/savepoints", config.directory().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize valid YAML with required field only")
        void shouldDeserializeValidYamlWithRequiredOnly() throws Exception {
            var yaml = "savepoint-path: /tmp/savepoint-1\n";

            var config = mapper.readValue(yaml, SavepointRestoreProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("/tmp/savepoint-1", config.savepointPath().orElseThrow()),
                () -> assertTrue(config.allowNonRestoredState().isEmpty()),
                () -> assertTrue(config.restoreMode().isEmpty()),
                () -> assertTrue(config.directory().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize valid YAML with directory only")
        void shouldDeserializeValidYamlWithDirectoryOnly() throws Exception {
            var yaml = "directory: /tmp/savepoints\n";

            var config = mapper.readValue(yaml, SavepointRestoreProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertTrue(config.savepointPath().isEmpty()),
                () -> assertTrue(config.allowNonRestoredState().isEmpty()),
                () -> assertTrue(config.restoreMode().isEmpty()),
                () -> assertEquals("/tmp/savepoints", config.directory().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid configuration")
        void shouldPassValidation() {
            var config = new SavepointRestoreProperties("/tmp/savepoint-1", true, RestoreMode.NO_CLAIM, "/tmp/savepoints");

            var violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Should have no violations");
        }

        @Test
        @DisplayName("Should pass validation when only savepointPath is specified")
        void shouldPassValidationWhenOnlySavepointPathIsSpecified() {
            var config = new SavepointRestoreProperties("/tmp/savepoint-1", null, null, null);

            var violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Should have no violations");
        }

        @Test
        @DisplayName("Should pass validation when only directory is specified")
        void shouldPassValidationWhenOnlyDirectoryIsSpecified() {
            var config = new SavepointRestoreProperties(null, null, null, "/tmp/savepoints");

            var violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Should have no violations");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "\t", "\n", "   "})
        @DisplayName("Should reject empty and whitespace-only savepointPath")
        void shouldFailValidationWhenSavepointPathIsBlank(String savepointPath) {
            var config = new SavepointRestoreProperties(savepointPath, true, RestoreMode.CLAIM, "/tmp/savepoints");

            var violations = validator.validate(config);

            assertEquals(1, violations.size());
            assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("savepointPath")
            ));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "\t", "\n", "   "})
        @DisplayName("Should reject empty and whitespace-only directories")
        void shouldFailValidationWhenDirectoryIsBlank(String directory) {
            var config = new SavepointRestoreProperties("/tmp/savepoint-1", true, RestoreMode.CLAIM, directory);

            var violations = validator.validate(config);

            assertEquals(1, violations.size());
            assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("directory")
            ));
        }

        @Test
        @DisplayName("Should fail validation when both savepointPath and directory are null")
        void shouldFailValidationWhenBothSavepointPathAndDirectoryAreNull() {
            var config = new SavepointRestoreProperties(null, true, RestoreMode.CLAIM, null);

            var violations = validator.validate(config);

            assertEquals(1, violations.size());
            assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("savepoint-path")
            ));
        }
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return expected values from getters")
        void testGetters() {
            var config = new SavepointRestoreProperties("/path/to/savepoint", false, RestoreMode.NO_CLAIM, "/tmp/savepoints");

            assertAll(
                () -> assertEquals("/path/to/savepoint", config.savepointPath().orElseThrow()),
                () -> assertTrue(config.allowNonRestoredState().isPresent()),
                () -> assertFalse(config.allowNonRestoredState().get()),
                () -> assertTrue(config.restoreMode().isPresent()),
                () -> assertEquals(RestoreMode.NO_CLAIM, config.restoreMode().get()),
                () -> assertEquals("/tmp/savepoints", config.directory().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should return empty Optionals when optional fields are null")
        void shouldReturnEmptyOptionalsWhenOptionalFieldsAreNull() {
            var config = new SavepointRestoreProperties(null, null, null, null);

            assertAll(
                () -> assertTrue(config.savepointPath().isEmpty()),
                () -> assertTrue(config.directory().isEmpty()),
                () -> assertTrue(config.allowNonRestoredState().isEmpty()),
                () -> assertTrue(config.restoreMode().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should satisfy equals and hashCode contract")
        void testEqualsAndHashCode() {
            var config1 = new SavepointRestoreProperties("/path", true, RestoreMode.CLAIM, null);
            var config2 = new SavepointRestoreProperties("/path", true, RestoreMode.CLAIM, null);
            var config3 = new SavepointRestoreProperties("/other", false, RestoreMode.NO_CLAIM, null);
            var configWithDirectory = new SavepointRestoreProperties("/path", true, RestoreMode.CLAIM, "/tmp/savepoints");

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, config3),
                () -> assertNotEquals(config1, configWithDirectory),
                () -> assertNotEquals(configWithDirectory, config1),
                () -> assertNotEquals(null, config1),
                () -> assertNotEquals("string", config1)
            );
        }
    }
}
