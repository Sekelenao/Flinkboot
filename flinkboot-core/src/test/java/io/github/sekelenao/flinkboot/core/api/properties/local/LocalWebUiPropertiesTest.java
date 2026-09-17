package io.github.sekelenao.flinkboot.core.api.properties.local;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocalWebUiProperties")
class LocalWebUiPropertiesTest {

    private static final YAMLMapper mapper = YAMLMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .findAndAddModules()
        .build();

    private static final Validator validator;

    static {
        try (var factory = buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should instantiate with valid arguments")
        void shouldInstantiateWithValidArguments() {
            var config = new LocalWebUiProperties(true, 8081, "localhost");
            assertNotNull(config);
        }

        @Test
        @DisplayName("Should instantiate with null arguments")
        void shouldInstantiateWithNullArguments() {
            var config = new LocalWebUiProperties(null, null, null);
            assertNotNull(config);
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        @DisplayName("Should return expected values when fields are provided")
        void shouldReturnExpectedValuesWhenFieldsAreProvided() {
            var config = new LocalWebUiProperties(false, 9090, "0.0.0.0");
            assertAll(
                () -> assertTrue(config.enabled().isPresent()),
                () -> assertFalse(config.enabled().get()),
                () -> assertTrue(config.port().isPresent()),
                () -> assertEquals(9090, config.port().getAsInt()),
                () -> assertTrue(config.bindAddress().isPresent()),
                () -> assertEquals("0.0.0.0", config.bindAddress().get())
            );
        }

        @Test
        @DisplayName("Should return empty optionals when fields are null")
        void shouldReturnEmptyOptionalsWhenFieldsAreNull() {
            var config = new LocalWebUiProperties(null, null, null);
            assertAll(
                () -> assertTrue(config.enabled().isEmpty()),
                () -> assertTrue(config.port().isEmpty()),
                () -> assertTrue(config.bindAddress().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest
        @ValueSource(ints = {0, 8081, 65535})
        @DisplayName("Should pass validation when port is within valid range")
        void shouldPassValidationWhenPortIsWithinValidRange(int port) {
            var config = new LocalWebUiProperties(true, port, "127.0.0.1");
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no violations for valid port");
        }

        @Test
        @DisplayName("Should pass validation when port is null")
        void shouldPassValidationWhenPortIsNull() {
            var config = new LocalWebUiProperties(true, null, "127.0.0.1");
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no violations when port is null");
        }

        @Test
        @DisplayName("Should pass validation when bindAddress is null")
        void shouldPassValidationWhenBindAddressIsNull() {
            var config = new LocalWebUiProperties(true, 8081, null);
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no violations when bindAddress is null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Should fail validation when bindAddress is empty or blank")
        void shouldFailValidationWhenBindAddressIsBlank(String bindAddress) {
            var config = new LocalWebUiProperties(true, 8081, bindAddress);
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size(), "Should have exactly 1 violation for blank bindAddress"),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("bindAddress")
                        && v.getMessage().equals("must not be blank")
                ), "Violation should target 'bindAddress' with message 'must not be blank'")
            );
        }

        @Test
        @DisplayName("Should fail validation when enabled is null")
        void shouldFailValidationWhenEnabledIsNull() {
            var config = new LocalWebUiProperties(null, 8081, "localhost");
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size(), "Should have exactly 1 violation for null enabled"),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("enabled")),
                    "Violation should be on the enabled field")
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 65536})
        @DisplayName("Should fail validation when port is outside valid range")
        void shouldFailValidationWhenPortIsOutsideValidRange(int port) {
            var config = new LocalWebUiProperties(true, port, "localhost");
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size(), "Should have exactly 1 violation for invalid port"),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("port")),
                    "Violation should target 'port'")
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should deserialize valid YAML with all fields")
        void shouldDeserializeValidYamlWithAllFields() throws Exception {
            var yaml = "enabled: true\n" +
                "port: 8081\n" +
                "bind-address: localhost\n";
            var config = mapper.readValue(yaml, LocalWebUiProperties.class);
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
        @DisplayName("Should deserialize valid YAML with only enabled field")
        void shouldDeserializeValidYamlWithOnlyEnabledField() throws Exception {
            var yaml = "enabled: false\n";
            var config = mapper.readValue(yaml, LocalWebUiProperties.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertTrue(config.enabled().isPresent()),
                () -> assertFalse(config.enabled().get()),
                () -> assertTrue(config.port().isEmpty()),
                () -> assertTrue(config.bindAddress().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize valid empty YAML")
        void shouldDeserializeValidEmptyYaml() throws Exception {
            var yaml = "{}\n";
            var config = mapper.readValue(yaml, LocalWebUiProperties.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertTrue(config.enabled().isEmpty()),
                () -> assertTrue(config.port().isEmpty()),
                () -> assertTrue(config.bindAddress().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should satisfy equals and hashCode contract")
        void shouldSatisfyEqualsAndHashCodeContract() {
            var config1 = new LocalWebUiProperties(true, 8081, "localhost");
            var config2 = new LocalWebUiProperties(true, 8081, "localhost");
            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config2, config1),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(null, config1),
                () -> assertNotEquals("string", config1)
            );
        }

        @Test
        @DisplayName("Should not equal when individual fields differ")
        void shouldNotEqualWhenIndividualFieldsDiffer() {
            var base = new LocalWebUiProperties(true, 8081, "localhost");
            var diffEnabled = new LocalWebUiProperties(false, 8081, "localhost");
            var diffPort = new LocalWebUiProperties(true, 9090, "localhost");
            var diffBindAddress = new LocalWebUiProperties(true, 8081, "0.0.0.0");
            var nullEnabled = new LocalWebUiProperties(null, 8081, "localhost");
            var nullPort = new LocalWebUiProperties(true, null, "localhost");
            var nullBindAddress = new LocalWebUiProperties(true, 8081, null);
            assertAll(
                () -> assertNotEquals(base, diffEnabled),
                () -> assertNotEquals(base, diffPort),
                () -> assertNotEquals(base, diffBindAddress),
                () -> assertNotEquals(base, nullEnabled),
                () -> assertNotEquals(base, nullPort),
                () -> assertNotEquals(base, nullBindAddress)
            );
        }

        @Test
        @DisplayName("Should return meaningful string representation")
        void shouldReturnMeaningfulStringRepresentation() {
            var config = new LocalWebUiProperties(true, 8081, "localhost");
            var str = config.toString();
            assertAll(
                () -> assertTrue(str.contains("LocalWebUiProperties")),
                () -> assertTrue(str.contains("enabled=true")),
                () -> assertTrue(str.contains("port=8081")),
                () -> assertTrue(str.contains("bindAddress='localhost'"))
            );
        }
    }
}
