package io.github.sekelenao.internal.yaml;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sekelenao.api.exception.configuration.ConfigurationValidationException;
import io.github.sekelenao.api.exception.configuration.YamlParsingException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JacksonException;

@DisplayName("YamlParser")
public class YamlParserTest {

    static final class TestConfig {
        @NotBlank
        private final String name;

        @Min(1)
        private final int value;

        @JsonCreator
        public TestConfig(
            @JsonProperty("name") String name,
            @JsonProperty("value") int value
        ) {
            this.name = name;
            this.value = value;
        }

        public String name() {
            return name;
        }

        public int value() {
            return value;
        }
    }

    @Nested
    @DisplayName("Parse")
    class Parse {

        @Test
        @DisplayName("Should parse valid YAML configuration correctly")
        void shouldParseValidYaml() {
            var yamlContent = "name: \"Flink Job\"\nvalue: 42\n";
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
        @DisplayName("Should parse twice in separate instances")
        void shouldParseTwice() {
            var yamlContent = "name: \"Flink Job\"\nvalue: 42\n";
            var bytes = yamlContent.getBytes(StandardCharsets.UTF_8);
            try (var parser1 = new YamlParser()) {
                var config1 = parser1.parse(new ByteArrayInputStream(bytes), TestConfig.class);
                assertNotNull(config1);
            }
            try (var parser2 = new YamlParser()) {
                var config2 = parser2.parse(new ByteArrayInputStream(bytes), TestConfig.class);
                assertNotNull(config2);
            }
        }

        @Test
        @DisplayName("Should throw ConfigurationValidationException when validation fails")
        void shouldThrowExceptionWhenValidationFails() {
            var yamlContent = "name: \"\"\nvalue: 0\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                var exception = assertThrows(ConfigurationValidationException.class, () -> parser.parse(stream, TestConfig.class));
                assertAll(
                    () -> assertNotNull(exception.getMessage()),
                    () -> assertTrue(exception.getMessage().contains("name")),
                    () -> assertTrue(exception.getMessage().contains("value"))
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
        @DisplayName("Should throw YamlParsingException when YAML is malformed")
        void shouldThrowExceptionWhenYamlIsMalformed() {
            var yamlContent = "name: \"Flink Job\nvalue: invalid_number\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                var exception = assertThrows(YamlParsingException.class, () -> parser.parse(stream, TestConfig.class));
                assertAll(
                    () -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
                    () -> assertNotNull(exception.getCause(), "Exception cause should not be null"),
                    () -> assertInstanceOf(JacksonException.class, exception.getCause(), "Exception cause should be a JacksonException")
                );
            }
        }
    }
}
