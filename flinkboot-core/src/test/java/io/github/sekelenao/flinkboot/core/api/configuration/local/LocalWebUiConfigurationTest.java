package io.github.sekelenao.flinkboot.core.api.configuration.local;

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

@DisplayName("LocalWebUiConfiguration Tests")
class LocalWebUiConfigurationTest {

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
            var yaml = "enabled: true\n" +
                "port: 8081\n" +
                "bind-address: localhost\n";

            var config = mapper.readValue(yaml, LocalWebUiConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertTrue(config.enabled().isPresent()),
                () -> assertTrue(config.enabled().get()),
                () -> assertTrue(config.port().isPresent()),
                () -> assertEquals(8081, config.port().getAsInt()),
                () -> assertTrue(config.bindAddress().isPresent()),
                () -> assertEquals("localhost", config.bindAddress().get())
            );
        }

        @Test
        @DisplayName("Should deserialize valid empty YAML")
        void shouldDeserializeValidEmptyYaml() throws Exception {
            var yaml = "{}\n";

            var config = mapper.readValue(yaml, LocalWebUiConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertTrue(config.enabled().isEmpty()),
                () -> assertTrue(config.port().isEmpty()),
                () -> assertTrue(config.bindAddress().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid configuration")
        void shouldPassValidation() {
            var config = new LocalWebUiConfiguration(true, 8081, "127.0.0.1");

            Set<ConstraintViolation<LocalWebUiConfiguration>> violations = validator.validate(config);

            assertTrue(violations.isEmpty(), "Should have no violations");
        }

        @Test
        @DisplayName("Should fail validation when port is negative or zero")
        void shouldFailValidationWhenPortIsInvalid() {
            var config = new LocalWebUiConfiguration(true, -1, "localhost");

            Set<ConstraintViolation<LocalWebUiConfiguration>> violations = validator.validate(config);

            assertFalse(violations.isEmpty(), "Should have validation violations for invalid port");
        }
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return expected values from getters")
        void testGetters() {
            var config = new LocalWebUiConfiguration(false, 9090, "0.0.0.0");

            assertAll(
                () -> assertTrue(config.enabled().isPresent()),
                () -> assertFalse(config.enabled().get()),
                () -> assertTrue(config.port().isPresent()),
                () -> assertEquals(9090, config.port().getAsInt()),
                () -> assertTrue(config.bindAddress().isPresent()),
                () -> assertEquals("0.0.0.0", config.bindAddress().get())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should satisfy equals and hashCode contract")
        void testEqualsAndHashCode() {
            var config1 = new LocalWebUiConfiguration(true, 8081, "localhost");
            var config2 = new LocalWebUiConfiguration(true, 8081, "localhost");
            var config3 = new LocalWebUiConfiguration(false, 8082, "0.0.0.0");

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
