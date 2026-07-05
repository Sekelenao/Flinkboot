package io.github.sekelenao.internal.yaml;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sekelenao.api.exception.ConfigurationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

@DisplayName("YamlParser")
class YamlParserTest {

    private record TestConfig(
        @NotBlank
        String name,
        @Min(1)
        int value
    ) {}

    @Nested
    @DisplayName("Parse")
    class Parse {

        @Test
        @DisplayName("Should parse valid YAML configuration correctly")
        void shouldParseValidYaml() {
            String yamlContent = """
                name: "Flink Job"
                value: 42
                """;
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                var config = parser.parse(stream, TestConfig.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertEquals("Flink Job", config.name()),
                    () -> assertEquals(42, config.value())
                );
            }
        }

        @Test
        @DisplayName("Should throw ConfigurationException when validation fails")
        void shouldThrowExceptionWhenValidationFails() {
            String yamlContent = """
                name: ""
                value: 0
                """;
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                var exception = assertThrows(ConfigurationException.class, () -> parser.parse(stream, TestConfig.class));
                assertAll(
                    () -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
                    () -> assertTrue(exception.getMessage().contains("name"), "Exception message should mention the invalid 'name' field"),
                    () -> assertTrue(exception.getMessage().contains("value"), "Exception message should mention the invalid 'value' field")
                );
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when source or class is null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var stream = new ByteArrayInputStream("".getBytes());
            try (var parser = new YamlParser()) {
                assertAll(
                    () -> assertThrows(NullPointerException.class, () -> parser.parse(null, TestConfig.class)),
                    () -> assertThrows(NullPointerException.class, () -> parser.parse(stream, null))
                );
            }
        }

        @Test
        @DisplayName("Should throw JacksonException when YAML is malformed")
        void shouldThrowExceptionWhenYamlIsMalformed() {
            String yamlContent = """
                name: "Flink Job
                value: invalid_number
                """;
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                assertThrows(JacksonException.class, () -> parser.parse(stream, TestConfig.class));
            }
        }
    }
}
