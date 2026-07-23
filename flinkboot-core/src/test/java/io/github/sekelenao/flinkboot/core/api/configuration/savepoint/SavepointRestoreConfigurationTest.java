package io.github.sekelenao.flinkboot.core.api.configuration.savepoint;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SavepointRestoreConfiguration Tests")
class SavepointRestoreConfigurationTest {

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
                "restore-mode: CLAIM\n";

            var config = mapper.readValue(yaml, SavepointRestoreConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("/tmp/savepoint-1", config.savepointPath()),
                () -> assertTrue(config.allowNonRestoredState().isPresent()),
                () -> assertTrue(config.allowNonRestoredState().get()),
                () -> assertTrue(config.restoreMode().isPresent()),
                () -> assertEquals(RestoreMode.CLAIM, config.restoreMode().get())
            );
        }

        @Test
        @DisplayName("Should deserialize valid YAML with required field only")
        void shouldDeserializeValidYamlWithRequiredOnly() throws Exception {
            var yaml = "savepoint-path: /tmp/savepoint-1\n";

            var config = mapper.readValue(yaml, SavepointRestoreConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("/tmp/savepoint-1", config.savepointPath()),
                () -> assertTrue(config.allowNonRestoredState().isEmpty()),
                () -> assertTrue(config.restoreMode().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid configuration")
        void shouldPassValidation() {
            var config = new SavepointRestoreConfiguration("/tmp/savepoint-1", true, RestoreMode.NO_CLAIM);

            Set<ConstraintViolation<SavepointRestoreConfiguration>> violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Should have no violations");
        }

        @Test
        @DisplayName("Should fail validation when savepointPath is blank")
        void shouldFailValidationWhenSavepointPathIsBlank() {
            var config = new SavepointRestoreConfiguration("   ", true, RestoreMode.CLAIM);

            Set<ConstraintViolation<SavepointRestoreConfiguration>> violations = validator.validate(config);

            assertFalse(violations.isEmpty(), "Should have validation violations");
        }

        @Test
        @DisplayName("Should throw NullPointerException when savepointPath is null")
        void shouldThrowExceptionWhenSavepointPathIsNull() {
            assertThrows(NullPointerException.class, () -> new SavepointRestoreConfiguration(null, true, RestoreMode.CLAIM));
        }
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return expected values from getters")
        void testGetters() {
            var config = new SavepointRestoreConfiguration("/path/to/savepoint", false, RestoreMode.LEGACY);

            assertAll(
                () -> assertEquals("/path/to/savepoint", config.savepointPath()),
                () -> assertTrue(config.allowNonRestoredState().isPresent()),
                () -> assertFalse(config.allowNonRestoredState().get()),
                () -> assertTrue(config.restoreMode().isPresent()),
                () -> assertEquals(RestoreMode.LEGACY, config.restoreMode().get())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should satisfy equals and hashCode contract")
        void testEqualsAndHashCode() {
            var config1 = new SavepointRestoreConfiguration("/path", true, RestoreMode.CLAIM);
            var config2 = new SavepointRestoreConfiguration("/path", true, RestoreMode.CLAIM);
            var config3 = new SavepointRestoreConfiguration("/other", false, RestoreMode.NO_CLAIM);

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, config3),
                () -> assertNotEquals(null, config1),
                () -> assertNotEquals("string", config1)
            );
        }
    }
}
