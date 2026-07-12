package io.github.sekelenao.flinkboot.core.internal.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.YamlParsingException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("YamlParser")
public class YamlParserTest {

    enum JobType {
        BATCH, STREAMING
    }

    static final class TestConfigWithEnum {
        private final JobType type;

        @JsonCreator
        public TestConfigWithEnum(@JsonProperty("type") JobType type) {
            this.type = type;
        }

        public JobType type() {
            return type;
        }
    }

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

    static final class TestConfigWithList {
        private final List<String> items;

        @JsonCreator
        public TestConfigWithList(@JsonProperty("items") List<String> items) {
            this.items = items;
        }

        public List<String> items() {
            return items;
        }
    }

    @Nested
    @DisplayName("Parse")
    class Parse {

        @ParameterizedTest
        @ValueSource(strings = {
            "name: \"Flink Job\"\nvalue: 42\n",
            "name: \"Flink Job\"\nvalue: 42\nextraField: \"ignoredValue\"\n",
            "NAME: \"Flink Job\"\nVALUE: 42\n"
        })
        @DisplayName("Should successfully parse YAML configuration with standard, unknown, or case-insensitive properties")
        void shouldParseYamlConfigurations(String yamlContent) {
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            try (var parser = new YamlParser()) {
                parser.parse(stream);
                var config = parser.convertTo(TestConfig.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertEquals("Flink Job", config.name()),
                    () -> assertEquals(42, config.value())
                );
            }
        }

        @Test
        @DisplayName("Should successfully parse YAML with case-insensitive enums by default")
        void shouldParseCaseInsensitiveEnums() {
            var yamlContent = "type: \"streaming\"\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                parser.parse(stream);
                var config = parser.convertTo(TestConfigWithEnum.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertEquals(JobType.STREAMING, config.type())
                );
            }
        }

        @Test
        @DisplayName("Should apply custom mapper configuration via builder consumer")
        void shouldApplyCustomConfiguration() throws IOException {
            var yamlContent = "type: \"INVALID_TYPE\"\n";
            var bytes = yamlContent.getBytes(StandardCharsets.UTF_8);
            Consumer<YAMLMapper.Builder> additionalConfigurations = builder -> {
                builder.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
            };
            try (var customizedParser = new YamlParser(additionalConfigurations); var inputStream = new ByteArrayInputStream(bytes)) {
                customizedParser.parse(inputStream);
                var config = customizedParser.convertTo(TestConfigWithEnum.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertNull(config.type())
                );
            }
            try (var defaultParser = new YamlParser(); var inputStream = new ByteArrayInputStream(bytes)) {
                defaultParser.parse(inputStream);
                assertThrows(YamlParsingException.class, () -> defaultParser.convertTo(TestConfigWithEnum.class));
            }
        }

        @Test
        @DisplayName("Should parse twice in separate instances")
        void shouldParseTwice() {
            var yamlContent = "name: \"Flink Job\"\nvalue: 42\n";
            var bytes = yamlContent.getBytes(StandardCharsets.UTF_8);
            try (var parser1 = new YamlParser()) {
                parser1.parse(new ByteArrayInputStream(bytes));
                var config1 = parser1.convertTo(TestConfig.class);
                assertNotNull(config1);
            }
            try (var parser2 = new YamlParser()) {
                parser2.parse(new ByteArrayInputStream(bytes));
                var config2 = parser2.convertTo(TestConfig.class);
                assertNotNull(config2);
            }
        }

        @Test
        @DisplayName("Should merge configurations when parsed multiple times")
        void shouldMergeConfigurationsWhenParsedMultipleTimes() {
            var baseYaml = "name: \"BaseApp\"\nvalue: 42\n";
            var overrideYaml = "value: 100\n";

            try (var parser = new YamlParser()) {
                parser.parse(new ByteArrayInputStream(baseYaml.getBytes(StandardCharsets.UTF_8)));
                parser.parse(new ByteArrayInputStream(overrideYaml.getBytes(StandardCharsets.UTF_8)));

                var config = parser.convertTo(TestConfig.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertEquals("BaseApp", config.name()),
                    () -> assertEquals(100, config.value())
                );
            }
        }

        @Test
        @DisplayName("Should merge configurations when fields are spread across multiple documents")
        void shouldMergeConfigurationsWithSpreadFields() {
            var firstYaml = "name: \"BaseApp\"\n";
            var secondYaml = "value: 42\n";

            try (var parser = new YamlParser()) {
                parser.parse(new ByteArrayInputStream(firstYaml.getBytes(StandardCharsets.UTF_8)));
                parser.parse(new ByteArrayInputStream(secondYaml.getBytes(StandardCharsets.UTF_8)));

                var config = parser.convertTo(TestConfig.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertEquals("BaseApp", config.name()),
                    () -> assertEquals(42, config.value())
                );
            }
        }

        @Test
        @DisplayName("Should throw ConfigurationValidationException when validation fails")
        void shouldThrowExceptionWhenValidationFails() {
            var yamlContent = "name: \"\"\nvalue: 0\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            try (var parser = new YamlParser()) {
                parser.parse(stream);
                var exception = assertThrows(ConfigurationValidationException.class, () -> parser.convertTo(TestConfig.class));
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
            try (var parser = new YamlParser()) {
                assertAll(
                    () -> assertThrows(NullPointerException.class, () -> parser.parse(null)),
                    () -> assertThrows(NullPointerException.class, () -> parser.convertTo(null))
                );
            }
        }

        @Test
        @DisplayName("Should throw YamlParsingException when YAML is malformed")
        void shouldThrowExceptionWhenYamlIsMalformed() {
            var yamlContent = "name: \"Flink Job\nvalue: invalid_number\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            try (var parser = new YamlParser()) {
                var exception = assertThrows(YamlParsingException.class, () -> parser.parse(stream));
                assertAll(
                    () -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
                    () -> assertNotNull(exception.getCause(), "Exception cause should not be null"),
                    () -> assertInstanceOf(JacksonException.class, exception.getCause(), "Exception cause should be a JacksonException")
                );
            }
        }

        @Test
        @DisplayName("Should throw YamlParsingException when YAML is empty")
        void shouldThrowExceptionWhenYamlIsEmpty() {
            var yamlContent = "";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            try (var parser = new YamlParser()) {
                assertThrows(YamlParsingException.class, () -> parser.parse(stream));
            }
        }

        @Test
        @DisplayName("Should throw YamlParsingException when YAML resolves to null")
        void shouldThrowExceptionWhenYamlIsNullLiteral() {
            var yamlContent = "null";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            try (var parser = new YamlParser()) {
                assertThrows(YamlParsingException.class, () -> parser.parse(stream));
            }
        }

        @Test
        @DisplayName("Should throw YamlParsingException when configuration resolves to null")
        void shouldThrowExceptionWhenConfigurationResolvesToNull() {
            try (var parser = new YamlParser()) {
                assertThrows(YamlParsingException.class, () -> parser.convertTo(Void.class));
            }
        }

        @Test
        @DisplayName("Should throw YamlParsingException when YAML root is a list")
        void shouldThrowExceptionWhenYamlRootIsNonObject() {
            var yamlContent = "- item1\n- item2\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            try (var parser = new YamlParser()) {
                assertThrows(YamlParsingException.class, () -> parser.parse(stream));
            }
        }

        @Test
        @DisplayName("Should demonstrate list merge behavior (appending elements)")
        void shouldAppendElementsWhenMergingLists() {
            var baseYaml = "items:\n  - \"item1\"\n  - \"item2\"\n";
            var overrideYaml = "items:\n  - \"item3\"\n";

            try (var parser = new YamlParser()) {
                parser.parse(new ByteArrayInputStream(baseYaml.getBytes(StandardCharsets.UTF_8)));
                parser.parse(new ByteArrayInputStream(overrideYaml.getBytes(StandardCharsets.UTF_8)));

                var config = parser.convertTo(TestConfigWithList.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertEquals(List.of("item1", "item2", "item3"), config.items())
                );
            }
        }
    }
}
