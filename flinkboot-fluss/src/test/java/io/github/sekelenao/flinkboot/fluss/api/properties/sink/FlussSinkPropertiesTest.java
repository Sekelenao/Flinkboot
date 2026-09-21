package io.github.sekelenao.flinkboot.fluss.api.properties.sink;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FlussSinkProperties")
class FlussSinkPropertiesTest {

    private static final YAMLMapper MAPPER = YAMLMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .findAndAddModules()
        .build();

    private static final Validator validator;
    static {
        try (var factory = buildDefaultValidatorFactory()) {
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
                "properties:\n" +
                "  client.writer.batch-size: 1mb\n";

            var config = MAPPER.readValue(yaml, FlussSinkProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9123"), config.bootstrapServers()),
                () -> assertEquals("my_db", config.database()),
                () -> assertEquals("my_table", config.table()),
                () -> assertEquals(Map.of("client.writer.batch-size", "1mb"), config.properties())
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

            var config = MAPPER.readValue(yaml, FlussSinkProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9123"), config.bootstrapServers()),
                () -> assertEquals("my_db", config.database()),
                () -> assertEquals("my_table", config.table()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass validation when all fields are valid")
        void shouldPassValidation() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                Map.of()
            );

            var violations = validator.validate(props);
            assertTrue(violations.isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail validation when name is blank")
        void shouldFailWhenNameIsBlank(String blankName) {
            var props = new FlussSinkProperties(
                blankName,
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                Map.of()
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail validation when database is blank")
        void shouldFailWhenDatabaseIsBlank(String blankDb) {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                blankDb,
                "my_table",
                Map.of()
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("database")))
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail validation when table is blank")
        void shouldFailWhenTableIsBlank(String blankTable) {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                blankTable,
                Map.of()
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("table")))
            );
        }

        @Test
        @DisplayName("Should fail validation when required fields are null")
        void shouldFailWhenRequiredFieldsAreNull() {
            var nullName = new FlussSinkProperties(null, List.of("s"), "d", "t", null);
            var nullServers = new FlussSinkProperties("n", null, "d", "t", null);
            var nullDb = new FlussSinkProperties("n", List.of("s"), null, "t", null);
            var nullTable = new FlussSinkProperties("n", List.of("s"), "d", null, null);

            assertAll(
                () -> {
                    var v = validator.validate(nullName);
                    assertEquals(1, v.size());
                    assertTrue(v.stream().anyMatch(vi -> vi.getPropertyPath().toString().equals("name")));
                },
                () -> {
                    var v = validator.validate(nullServers);
                    assertEquals(1, v.size());
                    assertTrue(v.stream().anyMatch(vi -> vi.getPropertyPath().toString().equals("bootstrapServers")));
                },
                () -> {
                    var v = validator.validate(nullDb);
                    assertEquals(1, v.size());
                    assertTrue(v.stream().anyMatch(vi -> vi.getPropertyPath().toString().equals("database")));
                },
                () -> {
                    var v = validator.validate(nullTable);
                    assertEquals(1, v.size());
                    assertTrue(v.stream().anyMatch(vi -> vi.getPropertyPath().toString().equals("table")));
                }
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers is empty")
        void shouldFailWhenBootstrapServersIsEmpty() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of(),
                "my_db",
                "my_table",
                Map.of()
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bootstrapServers")))
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers contains a blank element")
        void shouldFailWhenBootstrapServersContainsBlankElement() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("   "),
                "my_db",
                "my_table",
                Map.of()
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(
                    violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().startsWith("bootstrapServers"))
                )
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers contains a null element")
        void shouldFailWhenBootstrapServersContainsNullElement() {
            var props = new FlussSinkProperties(
                "my-sink",
                Collections.singletonList(null),
                "my_db",
                "my_table",
                Map.of()
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(
                    violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().startsWith("bootstrapServers"))
                )
            );
        }

        @Test
        @DisplayName("Should fail validation when properties contains a null key")
        void shouldFailWhenPropertiesContainsNullKey() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                Collections.singletonMap(null, "value")
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith("properties")))
            );
        }

        @Test
        @DisplayName("Should fail validation when properties contains a null value")
        void shouldFailWhenPropertiesContainsNullValue() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                Collections.singletonMap("key", null)
            );

            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().startsWith("properties")))
            );
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should instantiate successfully without throwing exceptions even with null parameters")
        void shouldInstantiateSuccessfully() {
            var props = new FlussSinkProperties(null, null, null, null, null);
            assertNotNull(props);
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        @DisplayName("Should return configured values from getters")
        void shouldReturnConfiguredValues() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                Map.of("k", "v")
            );

            assertAll(
                () -> assertEquals("my-sink", props.name()),
                () -> assertEquals(List.of("localhost:9123"), props.bootstrapServers()),
                () -> assertEquals("my_db", props.database()),
                () -> assertEquals("my_table", props.table()),
                () -> assertEquals(Map.of("k", "v"), props.properties())
            );
        }

        @Test
        @DisplayName("Should return empty collections when optional fields are null")
        void shouldReturnEmptyWhenFieldsNull() {
            var props = new FlussSinkProperties(
                "my-sink",
                null,
                "my_db",
                "my_table",
                null
            );

            assertAll(
                () -> assertTrue(props.bootstrapServers().isEmpty()),
                () -> assertTrue(props.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return unmodifiable properties map")
        void shouldReturnUnmodifiableProperties() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                new HashMap<>(Map.of("k", "v"))
            );

            var map = props.properties();
            assertThrows(UnsupportedOperationException.class, () -> map.put("new", "val"));
        }

        @Test
        @DisplayName("Should return unmodifiable bootstrap-servers list")
        void shouldReturnUnmodifiableBootstrapServers() {
            var props = new FlussSinkProperties(
                "my-sink",
                new ArrayList<>(List.of("localhost:9123")),
                "my_db",
                "my_table",
                Map.of()
            );

            var servers = props.bootstrapServers();
            assertThrows(UnsupportedOperationException.class, () -> servers.add("other:9123"));
        }

        @Test
        @DisplayName("Should return empty unmodifiable list when constructed with empty bootstrap-servers")
        void shouldReturnEmptyUnmodifiableListForEmptyBootstrapServers() {
            var props = new FlussSinkProperties(
                "sink",
                Collections.emptyList(),
                "db",
                "tbl",
                Map.of()
            );

            var servers = props.bootstrapServers();
            assertNotNull(servers, "bootstrapServers() should never return null");
            assertTrue(servers.isEmpty(), "Expected empty list when constructed with empty list");
            assertThrows(UnsupportedOperationException.class, () -> servers.add("x"), "Returned list must be unmodifiable");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should obey equals and hashCode contract across all fields")
        void shouldObeyContract() {
            var props1 = new FlussSinkProperties("s", List.of("h:9123"), "d", "t", Map.of("k", "v"));
            var props2 = new FlussSinkProperties("s", List.of("h:9123"), "d", "t", Map.of("k", "v"));
            var diffName = new FlussSinkProperties("other", List.of("h:9123"), "d", "t", Map.of("k", "v"));
            var diffServers = new FlussSinkProperties("s", List.of("other:9123"), "d", "t", Map.of("k", "v"));
            var diffDb = new FlussSinkProperties("s", List.of("h:9123"), "other_db", "t", Map.of("k", "v"));
            var diffTable = new FlussSinkProperties("s", List.of("h:9123"), "d", "other_tbl", Map.of("k", "v"));
            var diffProps = new FlussSinkProperties("s", List.of("h:9123"), "d", "t", Map.of("k2", "v2"));

            assertAll(
                () -> assertEquals(props1, props1),
                () -> assertEquals(props1, props2),
                () -> assertEquals(props2, props1),
                () -> assertEquals(props1.hashCode(), props2.hashCode()),
                () -> assertNotEquals(props1, diffName),
                () -> assertNotEquals(props1, diffServers),
                () -> assertNotEquals(props1, diffDb),
                () -> assertNotEquals(props1, diffTable),
                () -> assertNotEquals(props1, diffProps),
                () -> assertNotEquals(null, props1),
                () -> assertNotEquals("string", props1),
                () -> assertTrue(props1.toString().contains("FlussSinkProperties"))
            );
        }
    }
}
