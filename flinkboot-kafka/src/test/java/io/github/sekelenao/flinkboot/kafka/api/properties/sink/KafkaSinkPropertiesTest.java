package io.github.sekelenao.flinkboot.kafka.api.properties.sink;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaSinkProperties")
class KafkaSinkPropertiesTest {

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
                "  - localhost:9092\n" +
                "topic: my-topic\n" +
                "delivery-guarantee: EXACTLY_ONCE\n" +
                "transactional-id-prefix: my-prefix\n" +
                "properties:\n" +
                "  acks: all\n";

            var config = mapper.readValue(yaml, KafkaSinkProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-topic", config.topic()),
                () -> assertTrue(config.deliveryGuarantee().isPresent()),
                () -> assertEquals(KafkaDeliveryGuarantee.EXACTLY_ONCE, config.deliveryGuarantee().get()),
                () -> assertTrue(config.transactionalIdPrefix().isPresent()),
                () -> assertEquals("my-prefix", config.transactionalIdPrefix().get()),
                () -> assertEquals(Map.of("acks", "all"), config.properties())
            );
        }

        @Test
        @DisplayName("Should deserialize successfully from YAML without optional properties")
        void shouldDeserializeWithoutOptionalProperties() throws Exception {
            var yaml = "name: my-sink\n" +
                "bootstrap-servers:\n" +
                "  - localhost:9092\n" +
                "topic: my-topic\n";

            var config = mapper.readValue(yaml, KafkaSinkProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-topic", config.topic()),
                () -> assertTrue(config.deliveryGuarantee().isEmpty()),
                () -> assertTrue(config.transactionalIdPrefix().isEmpty()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid properties")
        void shouldPassValidation() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Should have no validation violations");
        }

        @Test
        @DisplayName("Should fail validation when required fields are null")
        void shouldFailWhenRequiredFieldsAreNull() {
            assertAll(
                () -> assertFalse(validator.validate(new KafkaSinkProperties(null, List.of("localhost:9092"), "t", null, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new KafkaSinkProperties("s", null, "t", null, null, null)).isEmpty()),
                () -> assertFalse(validator.validate(new KafkaSinkProperties("s", List.of("localhost:9092"), null, null, null, null)).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when name is blank")
        void shouldFailWhenNameIsBlank() {
            var config = new KafkaSinkProperties(
                "",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")))
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers is empty")
        void shouldFailWhenBootstrapServersIsEmpty() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of(),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("bootstrapServers")))
            );
        }

        @Test
        @DisplayName("Should fail validation when topic is blank")
        void shouldFailWhenTopicIsBlank() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("topic")))
            );
        }

        @Test
        @DisplayName("Should fail validation when transactional-id-prefix is blank")
        void shouldFailWhenTransactionalIdPrefixIsBlank() {
            var props = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "   ",
                null
            );
            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionalIdPrefix")))
            );
        }

        @Test
        @DisplayName("Should fail validation when transactional-id-prefix is empty")
        void shouldFailWhenTransactionalIdPrefixIsEmpty() {
            var props = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "",
                null
            );
            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("transactionalIdPrefix")))
            );
        }

        @Test
        @DisplayName("Should fail validation when transactional-id-prefix is specified for non-EXACTLY_ONCE guarantee")
        void shouldFailWhenTransactionalIdPrefixSpecifiedForNonExactlyOnce() {
            var props = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                "some-prefix",
                null
            );
            var violations = validator.validate(props);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("transactionalIdPrefix")
                        && v.getMessage().equals("transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE")
                ))
            );
        }

        @Test
        @DisplayName("Should fail validation when properties map contains null value")
        void shouldFailWhenPropertiesHasNullValue() {
            var properties = new HashMap<String, String>();
            properties.put("key", null);
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                properties
            );

            var violations = validator.validate(config);
            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("properties")))
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers contains a blank element")
        void shouldFailWhenBootstrapServersContainsBlankElement() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("   "),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            var violations = validator.validate(config);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(
                    violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().startsWith("bootstrapServers"))
                )
            );
        }

        @Test
        @DisplayName("Should fail validation when bootstrap-servers contains a null element")
        void shouldFailWhenBootstrapServersContainsNullElement() {
            var config = new KafkaSinkProperties(
                "my-sink",
                Collections.singletonList(null),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            var violations = validator.validate(config);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(
                    violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().startsWith("bootstrapServers"))
                )
            );
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("Should return expected values from getters when all parameters are present")
        void testGettersWithAllParameters() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "prefix",
                Map.of("key", "val")
            );

            assertAll(
                () -> assertEquals("my-sink", config.name()),
                () -> assertEquals(List.of("localhost:9092"), config.bootstrapServers()),
                () -> assertEquals("my-topic", config.topic()),
                () -> assertTrue(config.deliveryGuarantee().isPresent()),
                () -> assertEquals(KafkaDeliveryGuarantee.EXACTLY_ONCE, config.deliveryGuarantee().get()),
                () -> assertTrue(config.transactionalIdPrefix().isPresent()),
                () -> assertEquals("prefix", config.transactionalIdPrefix().get()),
                () -> assertEquals(Map.of("key", "val"), config.properties())
            );
        }

        @Test
        @DisplayName("Should return unmodifiable properties map")
        void shouldReturnUnmodifiableProperties() {
            var props = new HashMap<String, String>();
            props.put("k", "v");
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                props
            );

            var map = config.properties();
            assertThrows(UnsupportedOperationException.class, () -> map.put("new", "val"));
        }

        @Test
        @DisplayName("Should return unmodifiable bootstrap-servers list")
        void shouldReturnUnmodifiableBootstrapServers() {
            var servers = new ArrayList<String>();
            servers.add("localhost:9092");
            var config = new KafkaSinkProperties(
                "my-sink",
                servers,
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                Map.of()
            );

            var list = config.bootstrapServers();
            assertThrows(UnsupportedOperationException.class, () -> list.add("other:9092"));
        }

        @Test
        @DisplayName("Should return empty unmodifiable list when constructed with empty bootstrap-servers")
        void shouldReturnEmptyUnmodifiableListForEmptyBootstrapServers() {
            var config = new KafkaSinkProperties(
                "my-sink",
                Collections.emptyList(),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                Map.of()
            );

            var servers = config.bootstrapServers();
            assertNotNull(servers, "bootstrapServers() should never return null");
            assertTrue(servers.isEmpty(), "Expected empty list when constructed with empty list");
            assertThrows(UnsupportedOperationException.class, () -> servers.add("x"), "Returned list must be unmodifiable");
        }

        @Test
        @DisplayName("Should return empty unmodifiable list when bootstrapServers is null")
        void shouldReturnEmptyListWhenBootstrapServersIsNull() {
            var config = new KafkaSinkProperties(
                "my-sink",
                null,
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                Map.of()
            );

            var servers = config.bootstrapServers();
            assertNotNull(servers, "bootstrapServers() should never return null");
            assertTrue(servers.isEmpty(), "Expected empty list when constructed with null list");
            assertThrows(UnsupportedOperationException.class, () -> servers.add("x"), "Returned list must be unmodifiable");
        }

        @Test
        @DisplayName("Should return blank transactional ID prefix when it is blank")
        void shouldReturnBlankTransactionalIdPrefix() {
            var config = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "   ",
                Map.of()
            );

            assertEquals(Optional.of("   "), config.transactionalIdPrefix());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Equals and HashCode should work correctly")
        void testEqualsAndHashCode() {
            var config1 = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );
            var config2 = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "my-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );
            var configDiffTopic = new KafkaSinkProperties(
                "my-sink",
                List.of("localhost:9092"),
                "other-topic",
                KafkaDeliveryGuarantee.AT_LEAST_ONCE,
                null,
                null
            );

            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, null),
                () -> assertNotEquals(config1, "string-object"),
                () -> assertNotEquals(config1, configDiffTopic),
                () -> assertNotNull(config1.toString())
            );
        }
    }
}
