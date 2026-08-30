package io.github.sekelenao.flinkboot.fluss.api.properties.sink;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlussSinkProperties")
class FlussSinkPropertiesTest {

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
            var yaml = "name: my-sink\n" +
                "bootstrap-servers:\n" +
                "  - localhost:9123\n" +
                "database: my_db\n" +
                "table: my_table\n" +
                "batch-size: 1048576\n" +
                "batch-timeout: \"PT0.05S\"\n" +
                "properties:\n" +
                "  client.writer.bucket.batch.size: 1048576\n";

            var config = mapper.readValue(yaml, FlussSinkProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9123"), config.bootstrapServers()),
                () -> assertEquals("my_db", config.database()),
                () -> assertEquals("my_table", config.table()),
                () -> assertEquals(1048576L, config.batchSize().orElseThrow()),
                () -> assertEquals(Duration.ofMillis(50), config.batchTimeout().orElseThrow()),
                () -> assertEquals(Map.of("client.writer.bucket.batch.size", "1048576"), config.properties())
            );
        }

        @Test
        @DisplayName("Should deserialize successfully from YAML without optional properties")
        void shouldDeserializeWithoutOptionalProperties() throws Exception {
            var yaml = "name: my-sink\n" +
                "bootstrap-servers:\n" +
                "  - localhost:9123\n" +
                "database: my_db\n" +
                "table: my_table\n";

            var config = mapper.readValue(yaml, FlussSinkProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9123"), config.bootstrapServers()),
                () -> assertEquals("my_db", config.database()),
                () -> assertEquals("my_table", config.table()),
                () -> assertTrue(config.batchSize().isEmpty()),
                () -> assertTrue(config.batchTimeout().isEmpty()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation when all fields are valid")
        void shouldPassValidation() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                1024L,
                Duration.ofMillis(50),
                Map.of()
            );

            var violations = validator.validate(props);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation when optional batchTimeout is null")
        void shouldPassValidationWhenBatchTimeoutIsNull() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                1024L,
                null,
                Map.of()
            );

            var violations = validator.validate(props);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailWhenNameIsBlank() {
            var props = new FlussSinkProperties(
                "",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                1024L,
                Duration.ofMillis(50),
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when database is blank")
        void shouldFailWhenDatabaseIsBlank() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "",
                "my_table",
                1024L,
                Duration.ofMillis(50),
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when table is blank")
        void shouldFailWhenTableIsBlank() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "",
                1024L,
                Duration.ofMillis(50),
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when required fields are null")
        void shouldFailWhenRequiredFieldsAreNull() {
            assertAll(
                () -> assertFalse(validator.validate(new FlussSinkProperties(null, List.of("s"), "d", "t", null, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSinkProperties("n", null, "d", "t", null, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSinkProperties("n", List.of("s"), null, "t", null, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSinkProperties("n", List.of("s"), "d", null, null, null, null)).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when batchTimeout is negative")
        void shouldFailWhenBatchTimeoutIsNegative() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                1024L,
                Duration.ofSeconds(-1),
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should instantiate successfully without throwing exceptions even with null parameters")
        void shouldInstantiateSuccessfully() {
            var props = new FlussSinkProperties(null, null, null, null, null, null, null);
            assertNotNull(props);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should obey equals and hashCode contract")
        void shouldObeyContract() {
            var props1 = new FlussSinkProperties("s", List.of("h:9123"), "d", "t", 100L, Duration.ofMillis(50), Map.of("k", "v"));
            var props2 = new FlussSinkProperties("s", List.of("h:9123"), "d", "t", 100L, Duration.ofMillis(50), Map.of("k", "v"));
            var props3 = new FlussSinkProperties("other", List.of("h:9123"), "d", "t", 100L, Duration.ofMillis(50), Map.of("k", "v"));

            assertAll(
                () -> assertEquals(props1, props2),
                () -> assertEquals(props1.hashCode(), props2.hashCode()),
                () -> assertNotEquals(props1, props3),
                () -> assertNotEquals(null, props1),
                () -> assertNotEquals("string", props1),
                () -> assertTrue(props1.toString().contains("FlussSinkProperties"))
            );
        }
    }
}

