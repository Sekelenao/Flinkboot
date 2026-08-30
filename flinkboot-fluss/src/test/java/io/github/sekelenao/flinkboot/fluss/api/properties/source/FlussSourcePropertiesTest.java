package io.github.sekelenao.flinkboot.fluss.api.properties.source;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.fluss.api.exception.InvalidFlussSourcePropertiesException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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

@DisplayName("FlussSourceProperties")
class FlussSourcePropertiesTest {

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
            var yaml = "name: my-source\n" +
                "bootstrap-servers:\n" +
                "  - localhost:9123\n" +
                "database: my_db\n" +
                "table: my_table\n" +
                "startup-mode: TIMESTAMP\n" +
                "startup-timestamp: 1700000000000\n" +
                "properties:\n" +
                "  client.scanner.fetch.max-bytes: 1048576\n";

            var config = mapper.readValue(yaml, FlussSourceProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-source", config.name()),
                () -> assertEquals(List.of("localhost:9123"), config.bootstrapServers()),
                () -> assertEquals("my_db", config.database()),
                () -> assertEquals("my_table", config.table()),
                () -> assertEquals(FlussStartupMode.TIMESTAMP, config.startupMode()),
                () -> assertEquals(1700000000000L, config.startupTimestamp().orElseThrow()),
                () -> assertEquals(Map.of("client.scanner.fetch.max-bytes", "1048576"), config.properties())
            );
        }

        @Test
        @DisplayName("Should deserialize successfully from YAML without optional properties")
        void shouldDeserializeWithoutOptionalProperties() throws Exception {
            var yaml = "name: my-source\n" +
                "bootstrap-servers:\n" +
                "  - localhost:9123\n" +
                "database: my_db\n" +
                "table: my_table\n" +
                "startup-mode: EARLIEST\n";

            var config = mapper.readValue(yaml, FlussSourceProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-source", config.name()),
                () -> assertEquals(List.of("localhost:9123"), config.bootstrapServers()),
                () -> assertEquals("my_db", config.database()),
                () -> assertEquals("my_table", config.table()),
                () -> assertEquals(FlussStartupMode.EARLIEST, config.startupMode()),
                () -> assertTrue(config.startupTimestamp().isEmpty()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should deserialize case-insensitive enums")
        void shouldDeserializeCaseInsensitiveEnums() throws Exception {
            var yaml = "name: my-source\n" +
                "bootstrap-servers:\n" +
                "  - localhost:9123\n" +
                "database: my_db\n" +
                "table: my_table\n" +
                "startup-mode: latest\n";

            var config = mapper.readValue(yaml, FlussSourceProperties.class);
            assertEquals(FlussStartupMode.LATEST, config.startupMode());
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation when all fields are valid")
        void shouldPassValidation() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.EARLIEST,
                null,
                Map.of()
            );

            var violations = validator.validate(props);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailWhenNameIsBlank() {
            var props = new FlussSourceProperties(
                "",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.EARLIEST,
                null,
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when database is blank")
        void shouldFailWhenDatabaseIsBlank() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "",
                "my_table",
                FlussStartupMode.EARLIEST,
                null,
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when table is blank")
        void shouldFailWhenTableIsBlank() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "",
                FlussStartupMode.EARLIEST,
                null,
                Map.of()
            );

            var violations = validator.validate(props);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should throw InvalidFlussSourcePropertiesException when TIMESTAMP mode is used without timestamp")
        void shouldThrowWhenTimestampModeWithoutTimestamp() {
            var exception = assertThrows(InvalidFlussSourcePropertiesException.class, () ->
                new FlussSourceProperties(
                    "my-source",
                    List.of("localhost:9123"),
                    "my_db",
                    "my_table",
                    FlussStartupMode.TIMESTAMP,
                    null,
                    Map.of()
                )
            );
            assertEquals("startup-timestamp is required when startup-mode is TIMESTAMP", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw InvalidFlussSourcePropertiesException when timestamp is specified with non-TIMESTAMP mode")
        void shouldThrowWhenTimestampSpecifiedWithNonTimestampMode() {
            var exception = assertThrows(InvalidFlussSourcePropertiesException.class, () ->
                new FlussSourceProperties(
                    "my-source",
                    List.of("localhost:9123"),
                    "my_db",
                    "my_table",
                    FlussStartupMode.EARLIEST,
                    1700000000000L,
                    Map.of()
                )
            );
            assertEquals("startup-timestamp must not be specified when startup-mode is EARLIEST", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail validation when required fields are null")
        void shouldFailWhenRequiredFieldsAreNull() {
            assertAll(
                () -> assertFalse(validator.validate(new FlussSourceProperties(null, List.of("s"), "d", "t", FlussStartupMode.EARLIEST, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSourceProperties("n", null, "d", "t", FlussStartupMode.EARLIEST, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSourceProperties("n", List.of("s"), null, "t", FlussStartupMode.EARLIEST, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSourceProperties("n", List.of("s"), "d", null, FlussStartupMode.EARLIEST, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new FlussSourceProperties("n", List.of("s"), "d", "t", null, null, null)).isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should instantiate successfully without throwing exceptions even with null parameters")
        void shouldInstantiateSuccessfully() {
            var props = new FlussSourceProperties(null, null, null, null, null, null, null);
            assertNotNull(props);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should obey equals and hashCode contract")
        void shouldObeyContract() {
            var props1 = new FlussSourceProperties("s", List.of("h:9123"), "d", "t", FlussStartupMode.EARLIEST, null, Map.of("k", "v"));
            var props2 = new FlussSourceProperties("s", List.of("h:9123"), "d", "t", FlussStartupMode.EARLIEST, null, Map.of("k", "v"));
            var props3 = new FlussSourceProperties("other", List.of("h:9123"), "d", "t", FlussStartupMode.EARLIEST, null, Map.of("k", "v"));

            assertAll(
                () -> assertEquals(props1, props2),
                () -> assertEquals(props1.hashCode(), props2.hashCode()),
                () -> assertNotEquals(props1, props3),
                () -> assertNotEquals(null, props1),
                () -> assertNotEquals("string", props1),
                () -> assertTrue(props1.toString().contains("FlussSourceProperties"))
            );
        }
    }

    @Nested
    @DisplayName("Bootstrap servers")
    class BootstrapServersTests {

        @Test
        @DisplayName("Should return empty unmodifiable list when constructed with empty bootstrap-servers")
        void shouldReturnEmptyUnmodifiableListForEmptyBootstrapServers() {
            var props = new FlussSourceProperties(
                    "src",
                    Collections.emptyList(),
                    "db",
                    "tbl",
                    FlussStartupMode.EARLIEST,
                    null,
                    Map.of()
            );

            var servers = props.bootstrapServers();
            assertNotNull(servers, "bootstrapServers() should never return null");
            assertTrue(servers.isEmpty(), "Expected empty list when constructed with empty list");
            assertThrows(UnsupportedOperationException.class, () -> servers.add("x"), "Returned list must be unmodifiable");
        }

        @Test
        @DisplayName("Should return empty list when bootstrapServers is null (defensive getter)")
        void shouldHandleNullBootstrapServersInConstructor() {
            var props = new FlussSourceProperties(
                    "src",
                    null,
                    "db",
                    "tbl",
                    FlussStartupMode.EARLIEST,
                    null,
                    Map.of()
            );

            var servers = props.bootstrapServers();
            assertNotNull(servers, "bootstrapServers() should never return null even if supplied null in ctor");
            assertTrue(servers.isEmpty(), "Expected empty list when bootstrapServers is null");
        }
    }
}
