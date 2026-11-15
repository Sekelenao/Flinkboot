package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.flinkboot.core.api.exception.parsing.UnresolvedPropertyPlaceholderException;
import com.flinkboot.core.api.exception.parsing.YamlParsingException;
import com.flinkboot.core.internal.startup.EnvVarResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("YamlParser")
class YamlParserTest {

    private static final ParserFeatures STANDARD_FEATURES = ParserFeatures.builder()
        .permitOverride(false)
        .listMerging(false)
        .build();

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
        private final String name;
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

    static final class DatabaseConfig {
        private final String host;
        private final int port;
        private final List<String> options;

        @JsonCreator
        public DatabaseConfig(
            @JsonProperty("host") String host,
            @JsonProperty("port") int port,
            @JsonProperty("options") List<String> options
        ) {
            this.host = host;
            this.port = port;
            this.options = options;
        }

        public String host() { return host; }
        public int port() { return port; }
        public List<String> options() { return options; }
    }

    static final class ComplexConfig {
        private final String env;
        private final DatabaseConfig database;
        private final List<DatabaseConfig> replicas;

        @JsonCreator
        public ComplexConfig(
            @JsonProperty("env") String env,
            @JsonProperty("database") DatabaseConfig database,
            @JsonProperty("replicas") List<DatabaseConfig> replicas
        ) {
            this.env = env;
            this.database = database;
            this.replicas = replicas;
        }

        public String env() { return env; }
        public DatabaseConfig database() { return database; }
        public List<DatabaseConfig> replicas() { return replicas; }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create instance with custom YAMLMapper and ParserFeatures")
        void shouldCreateInstanceWithCustomMapperAndFeatures() {
            var mapper = new YAMLMapper();
            var parser = new YamlParser(mapper, STANDARD_FEATURES);
            assertNotNull(parser);
        }

        @Test
        @DisplayName("Should create instance with custom YAMLMapper, ParserFeatures and PlaceholderResolver")
        void shouldCreateInstanceWithCustomMapperFeaturesAndResolver() {
            var mapper = new YAMLMapper();
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> null));
            var parser = new YamlParser(mapper, STANDARD_FEATURES, resolver);
            assertNotNull(parser);
        }

        @Test
        @DisplayName("Should parse and convert successfully when created with custom YAMLMapper")
        void shouldParseSuccessfullyWithCustomMapper() {
            var mapper = new YAMLMapper();
            var yaml = "name: \"CustomMapperApp\"\nvalue: 99\n";
            var parser = new YamlParser(mapper, STANDARD_FEATURES);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("CustomMapperApp", config.name()),
                () -> assertEquals(99, config.value())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when constructor arguments are null")
        void shouldThrowExceptionWhenConstructorArgsAreNull() {
            var mapper = new YAMLMapper();
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> null));
            Consumer<YAMLMapper.Builder> customizer = builder -> {};

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(null)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser((Consumer<YAMLMapper.Builder>) null, STANDARD_FEATURES)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(customizer, null)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser((Consumer<YAMLMapper.Builder>) null, STANDARD_FEATURES, resolver)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(customizer, null, resolver)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(customizer, STANDARD_FEATURES, null)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser((YAMLMapper) null, STANDARD_FEATURES)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(mapper, null)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser((YAMLMapper) null, STANDARD_FEATURES, resolver)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(mapper, null, resolver)),
                () -> assertThrows(NullPointerException.class, () -> new YamlParser(mapper, STANDARD_FEATURES, null))
            );
        }
    }

    @Nested
    @DisplayName("Parse")
    class Parse {

        @ParameterizedTest
        @ValueSource(strings = {
            "name: \"Flink Job\"\nvalue: 42\n",
            "NAME: \"Flink Job\"\nVALUE: 42\n"
        })
        @DisplayName("Should successfully parse YAML configuration with standard or case-insensitive properties")
        void shouldParseYamlConfigurations(String yamlContent) {
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(stream);
            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("Flink Job", config.name()),
                () -> assertEquals(42, config.value())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when configuration contains unknown properties")
        void shouldThrowExceptionWhenYamlContainsUnknownProperties() {
            var yamlContent = "name: \"Flink Job\"\nvalue: 42\nextraField: \"value\"\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(stream);
            assertThrows(YamlParsingException.class, () -> parser.convertTo(TestConfig.class));
        }

        @Test
        @DisplayName("Should successfully parse YAML with case-insensitive enums by default")
        void shouldParseCaseInsensitiveEnums() {
            var yamlContent = "type: \"streaming\"\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));

            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(stream);
            var config = parser.convertTo(TestConfigWithEnum.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(JobType.STREAMING, config.type())
            );
        }

        @Test
        @DisplayName("Should apply custom mapper configuration via builder consumer")
        void shouldApplyCustomConfiguration() throws IOException {
            var yamlContent = "type: \"INVALID_TYPE\"\n";
            var bytes = yamlContent.getBytes(StandardCharsets.UTF_8);
            Consumer<YAMLMapper.Builder> additionalConfigurations = builder -> {
                builder.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
            };
            var customizedParser = new YamlParser(additionalConfigurations, STANDARD_FEATURES);
            try (var inputStream = new ByteArrayInputStream(bytes)) {
                customizedParser.parse(inputStream);
                var config = customizedParser.convertTo(TestConfigWithEnum.class);
                assertAll(
                    () -> assertNotNull(config),
                    () -> assertNull(config.type())
                );
            }
            var defaultParser = new YamlParser(STANDARD_FEATURES);
            try (var inputStream = new ByteArrayInputStream(bytes)) {
                defaultParser.parse(inputStream);
                assertThrows(YamlParsingException.class, () -> defaultParser.convertTo(TestConfigWithEnum.class));
            }
        }

        @Test
        @DisplayName("Should parse twice in separate instances")
        void shouldParseTwice() {
            var yamlContent = "name: \"Flink Job\"\nvalue: 42\n";
            var bytes = yamlContent.getBytes(StandardCharsets.UTF_8);
            var parser1 = new YamlParser(STANDARD_FEATURES);
            parser1.parse(new ByteArrayInputStream(bytes));
            var config1 = parser1.convertTo(TestConfig.class);
            assertNotNull(config1);

            var parser2 = new YamlParser(STANDARD_FEATURES);
            parser2.parse(new ByteArrayInputStream(bytes));
            var config2 = parser2.convertTo(TestConfig.class);
            assertNotNull(config2);
        }

        @Test
        @DisplayName("Should merge configurations when parsed multiple times")
        void shouldMergeConfigurationsWhenParsedMultipleTimes() {
            var baseYaml = "name: \"BaseApp\"\nvalue: 42\n";
            var overrideYaml = "value: 100\n";

            var features = ParserFeatures.builder().permitOverride(true).listMerging(false).build();
            var parser = new YamlParser(features);
            parser.parse(new ByteArrayInputStream(baseYaml.getBytes(StandardCharsets.UTF_8)));
            parser.parse(new ByteArrayInputStream(overrideYaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("BaseApp", config.name()),
                () -> assertEquals(100, config.value())
            );
        }

        @Test
        @DisplayName("Should merge configurations when fields are spread across multiple documents")
        void shouldMergeConfigurationsWithSpreadFields() {
            var firstYaml = "name: \"BaseApp\"\n";
            var secondYaml = "value: 42\n";

            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(new ByteArrayInputStream(firstYaml.getBytes(StandardCharsets.UTF_8)));
            parser.parse(new ByteArrayInputStream(secondYaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("BaseApp", config.name()),
                () -> assertEquals(42, config.value())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when source or class is null")
        void shouldThrowExceptionWhenParamsAreNull() {
            var parser = new YamlParser(STANDARD_FEATURES);
            assertAll(
                () -> assertThrows(NullPointerException.class, () -> parser.parse(null)),
                () -> assertThrows(NullPointerException.class, () -> parser.convertTo(null))
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when YAML is malformed")
        void shouldThrowExceptionWhenYamlIsMalformed() {
            var yamlContent = "name: \"Flink Job\nvalue: invalid_number\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            var exception = assertThrows(YamlParsingException.class, () -> parser.parse(stream));
            assertAll(
                () -> assertNotNull(exception.getMessage(), "Exception message should not be null"),
                () -> assertNotNull(exception.getCause(), "Exception cause should not be null"),
                () -> assertInstanceOf(JacksonException.class, exception.getCause(), "Exception cause should be a JacksonException")
            );
        }

        @Test
        @DisplayName("Should silently ignore empty YAML")
        void shouldSilentlyIgnoreEmptyYaml() {
            var yamlContent = "";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            assertDoesNotThrow(() -> parser.parse(stream));
        }

        @Test
        @DisplayName("Should silently ignore null YAML literal")
        void shouldSilentlyIgnoreNullYamlLiteral() {
            var yamlContent = "null";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            assertDoesNotThrow(() -> parser.parse(stream));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            " \n ",
            "   \n\n   ",
            "# Only comments\n",
            "# First comment\n# Second comment\n"
        })
        @DisplayName("Should silently ignore whitespace-only and comments-only YAML")
        void shouldSilentlyIgnoreWhitespaceOnlyAndCommentsOnlyYaml(String yamlContent) {
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            assertDoesNotThrow(() -> parser.parse(stream));
        }

        @Test
        @DisplayName("Should throw YamlParsingException when whitespace-only YAML contains tab characters")
        void shouldThrowExceptionWhenWhitespaceOnlyYamlContainsTabs() {
            var yamlContent = " \n\t ";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            assertThrows(YamlParsingException.class, () -> parser.parse(stream));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            " \n ",
            "   \n\n   ",
            "# Only comments\n",
            "# First comment\n# Second comment\n"
        })
        @DisplayName("Should preserve previously parsed configuration when parsing whitespace-only or comments-only YAML")
        void shouldPreserveConfigurationWhenParsingWhitespaceOnlyOrCommentsOnlyYaml(String yamlContent) {
            var baseYaml = "name: \"Flink Job\"\nvalue: 42\n";
            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(new ByteArrayInputStream(baseYaml.getBytes(StandardCharsets.UTF_8)));
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            assertDoesNotThrow(() -> parser.parse(stream));
            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("Flink Job", config.name()),
                () -> assertEquals(42, config.value())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when configuration resolves to null")
        void shouldThrowExceptionWhenConfigurationResolvesToNull() {
            var parser = new YamlParser(STANDARD_FEATURES);
            assertThrows(YamlParsingException.class, () -> parser.convertTo(Void.class));
        }

        @Test
        @DisplayName("Should wrap IllegalArgumentException raised during conversion into YamlParsingException")
        void shouldWrapIllegalArgumentExceptionRaisedDuringConversion() {
            var failingModule = new SimpleModule().addDeserializer(TestConfig.class, new FailingDeserializer());
            Consumer<YAMLMapper.Builder> additionalConfigurations = builder -> builder.addModule(failingModule);
            var yamlContent = "name: \"Flink Job\"\nvalue: 42\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(additionalConfigurations, STANDARD_FEATURES);
            parser.parse(stream);
            var exception = assertThrows(YamlParsingException.class, () -> parser.convertTo(TestConfig.class));
            assertAll(
                () -> assertEquals(FailingDeserializer.FAILING_DESERIALIZER_EXCEPTION_MESSAGE, exception.getMessage()),
                () -> assertInstanceOf(
                    IllegalArgumentException.class,
                    exception.getCause(),
                    "Exception cause should be the original IllegalArgumentException"
                )
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when YAML root is a list")
        void shouldThrowExceptionWhenYamlRootIsNonObject() {
            var yamlContent = "- item1\n- item2\n";
            var stream = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8));
            var parser = new YamlParser(STANDARD_FEATURES);
            assertThrows(YamlParsingException.class, () -> parser.parse(stream));
        }

        @Test
        @DisplayName("Should demonstrate list merge behavior (appending elements)")
        void shouldAppendElementsWhenMergingLists() {
            var baseYaml = "items:\n  - \"item1\"\n  - \"item2\"\n";
            var overrideYaml = "items:\n  - \"item3\"\n";

            var features = ParserFeatures.builder().permitOverride(false).listMerging(true).build();
            var parser = new YamlParser(features);
            parser.parse(new ByteArrayInputStream(baseYaml.getBytes(StandardCharsets.UTF_8)));
            parser.parse(new ByteArrayInputStream(overrideYaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(TestConfigWithList.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("item1", "item2", "item3"), config.items())
            );
        }
    }

    @Nested
    @DisplayName("Multi-Document YAML")
    class MultiDocument {

        @Test
        @DisplayName("Should merge all documents separated by ---")
        void shouldMergeAllDocuments() {
            var yaml = "env: \"prod\"\n" +
                "database:\n" +
                "  host: \"localhost\"\n" +
                "---\n" +
                "database:\n" +
                "  port: 5432\n" +
                "  options:\n" +
                "    - \"ssl=true\"\n" +
                "replicas: []\n";
            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(ComplexConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("prod", config.env()),
                () -> assertEquals("localhost", config.database().host()),
                () -> assertEquals(5432, config.database().port()),
                () -> assertEquals(List.of("ssl=true"), config.database().options()),
                () -> assertEquals(List.of(), config.replicas())
            );
        }

        @Test
        @DisplayName("Should skip empty documents between --- separators")
        void shouldSkipEmptyDocuments() {
            var yaml = "---\n" +
                "---\n" +
                "name: \"app\"\n" +
                "---\n" +
                "---\n" +
                "value: 42\n" +
                "---\n";
            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("app", config.name()),
                () -> assertEquals(42, config.value())
            );
        }

        @Test
        @DisplayName("Should allow overriding values across documents when permitOverride is true")
        void shouldAllowOverrideAcrossDocumentsWhenPermitted() {
            var yaml = "name: \"initial\"\n" +
                "value: 1\n" +
                "---\n" +
                "value: 99\n";
            var features = ParserFeatures.builder().permitOverride(true).listMerging(false).build();
            var parser = new YamlParser(features);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("initial", config.name()),
                () -> assertEquals(99, config.value())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when overriding across documents and permitOverride is false")
        void shouldThrowWhenOverridingAcrossDocumentsAndForbidden() {
            var yaml = "name: \"initial\"\n" +
                "value: 1\n" +
                "---\n" +
                "value: 99\n";
            var parser = new YamlParser(STANDARD_FEATURES);
            var stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            assertThrows(YamlParsingException.class, () -> parser.parse(stream));
        }

        @Test
        @DisplayName("Should merge lists across documents when listMerging is true")
        void shouldMergeListsAcrossDocumentsWhenPermitted() {
            var yaml = "items:\n" +
                "  - \"item1\"\n" +
                "---\n" +
                "items:\n" +
                "  - \"item2\"\n";
            var features = ParserFeatures.builder().permitOverride(false).listMerging(true).build();
            var parser = new YamlParser(features);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(TestConfigWithList.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("item1", "item2"), config.items())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when a subsequent document is not an object")
        void shouldThrowWhenSubsequentDocumentIsNotObject() {
            var yaml = "name: \"app\"\n" +
                "value: 1\n" +
                "---\n" +
                "- \"not-an-object\"\n";
            var parser = new YamlParser(STANDARD_FEATURES);
            var stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            assertThrows(YamlParsingException.class, () -> parser.parse(stream));
        }
    }

    @Nested
    @DisplayName("ParserFeatures Combinations")
    class ParserFeaturesCombinations {

        @Test
        @DisplayName("With permitOverride=false and listMerging=false: should throw exception on any override or list merge")
        void shouldThrowExceptionOnAnyOverrideOrListMerge() {
            var features = ParserFeatures.builder().permitOverride(false).listMerging(false).build();
            var yaml1 = "name: \"Base\"\nvalue: 42\n";
            var yaml2 = "value: 100\n";
            var yamlList1 = "items:\n  - \"a\"\n";
            var yamlList2 = "items:\n  - \"b\"\n";

            var parser1 = new YamlParser(features);
            parser1.parse(new ByteArrayInputStream(yaml1.getBytes(StandardCharsets.UTF_8)));
            var stream1 = new ByteArrayInputStream(yaml2.getBytes(StandardCharsets.UTF_8));
            assertThrows(YamlParsingException.class, () -> parser1.parse(stream1));

            var parser2 = new YamlParser(features);
            parser2.parse(new ByteArrayInputStream(yamlList1.getBytes(StandardCharsets.UTF_8)));
            var stream2 = new ByteArrayInputStream(yamlList2.getBytes(StandardCharsets.UTF_8));
            assertThrows(YamlParsingException.class, () -> parser2.parse(stream2));
        }

        @Test
        @DisplayName("With permitOverride=true and listMerging=false: should override scalars and replace lists")
        void shouldOverrideScalarsAndReplaceLists() {
            var features = ParserFeatures.builder().permitOverride(true).listMerging(false).build();
            var yaml1 = "name: \"Base\"\nvalue: 42\n";
            var yaml2 = "value: 100\n";
            var yamlList1 = "items:\n  - \"a\"\n";
            var yamlList2 = "items:\n  - \"b\"\n";

            var parser1 = new YamlParser(features);
            parser1.parse(new ByteArrayInputStream(yaml1.getBytes(StandardCharsets.UTF_8)));
            parser1.parse(new ByteArrayInputStream(yaml2.getBytes(StandardCharsets.UTF_8)));
            var config = parser1.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("Base", config.name()),
                () -> assertEquals(100, config.value())
            );

            var parser2 = new YamlParser(features);
            parser2.parse(new ByteArrayInputStream(yamlList1.getBytes(StandardCharsets.UTF_8)));
            parser2.parse(new ByteArrayInputStream(yamlList2.getBytes(StandardCharsets.UTF_8)));
            var configList = parser2.convertTo(TestConfigWithList.class);
            assertAll(
                () -> assertNotNull(configList),
                () -> assertEquals(List.of("b"), configList.items())
            );
        }

        @Test
        @DisplayName("With permitOverride=false and listMerging=true: should throw on scalar override but append lists")
        void shouldThrowOnScalarOverrideButAppendLists() {
            var features = ParserFeatures.builder().permitOverride(false).listMerging(true).build();
            var yaml1 = "name: \"Base\"\nvalue: 42\n";
            var yaml2 = "value: 100\n";
            var yamlList1 = "items:\n  - \"a\"\n";
            var yamlList2 = "items:\n  - \"b\"\n";

            var parser1 = new YamlParser(features);
            parser1.parse(new ByteArrayInputStream(yaml1.getBytes(StandardCharsets.UTF_8)));
            var stream1 = new ByteArrayInputStream(yaml2.getBytes(StandardCharsets.UTF_8));
            assertThrows(YamlParsingException.class, () -> parser1.parse(stream1));

            var parser2 = new YamlParser(features);
            parser2.parse(new ByteArrayInputStream(yamlList1.getBytes(StandardCharsets.UTF_8)));
            parser2.parse(new ByteArrayInputStream(yamlList2.getBytes(StandardCharsets.UTF_8)));
            var config = parser2.convertTo(TestConfigWithList.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("a", "b"), config.items())
            );
        }

        @Test
        @DisplayName("With permitOverride=true and listMerging=true: should override scalars and append lists")
        void shouldOverrideScalarsAndAppendLists() {
            var features = ParserFeatures.builder().permitOverride(true).listMerging(true).build();
            var yamlScalar1 = "name: \"Base\"\nvalue: 42\n";
            var yamlScalar2 = "value: 100\n";
            var yamlList1 = "items:\n  - \"a\"\n";
            var yamlList2 = "items:\n  - \"b\"\n";

            var parser1 = new YamlParser(features);
            parser1.parse(new ByteArrayInputStream(yamlScalar1.getBytes(StandardCharsets.UTF_8)));
            parser1.parse(new ByteArrayInputStream(yamlScalar2.getBytes(StandardCharsets.UTF_8)));
            var config = parser1.convertTo(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("Base", config.name()),
                () -> assertEquals(100, config.value())
            );

            var parser2 = new YamlParser(features);
            parser2.parse(new ByteArrayInputStream(yamlList1.getBytes(StandardCharsets.UTF_8)));
            parser2.parse(new ByteArrayInputStream(yamlList2.getBytes(StandardCharsets.UTF_8)));
            var configList = parser2.convertTo(TestConfigWithList.class);
            assertAll(
                () -> assertNotNull(configList),
                () -> assertEquals(List.of("a", "b"), configList.items())
            );
        }

        @Test
        @DisplayName("Should merge complex nested objects and lists correctly when merging is allowed")
        void shouldMergeComplexNestedObjectsAndLists() {
            var features = ParserFeatures.builder()
                .permitOverride(true)
                .listMerging(true)
                .build();

            var yaml1 = "env: \"dev\"\n" +
                "database:\n" +
                "  host: \"localhost\"\n" +
                "  port: 5432\n" +
                "  options:\n" +
                "    - \"ssl=true\"\n" +
                "replicas:\n" +
                "  - host: \"replica1\"\n" +
                "    port: 5433\n" +
                "    options:\n" +
                "      - \"readOnly=true\"\n";

            var yaml2 = "env: \"prod\"\n" +
                "database:\n" +
                "  host: \"db-prod\"\n" +
                "  options:\n" +
                "    - \"timeout=30\"\n" +
                "replicas:\n" +
                "  - host: \"replica2\"\n" +
                "    port: 5434\n" +
                "    options:\n" +
                "      - \"readOnly=true\"\n";

            var parser = new YamlParser(features);
            parser.parse(new ByteArrayInputStream(yaml1.getBytes(StandardCharsets.UTF_8)));
            parser.parse(new ByteArrayInputStream(yaml2.getBytes(StandardCharsets.UTF_8)));

            var config = parser.convertTo(ComplexConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("prod", config.env()),

                // Verify nested database object
                () -> assertNotNull(config.database()),
                () -> assertEquals("db-prod", config.database().host()),
                () -> assertEquals(5432, config.database().port()),
                () -> assertEquals(List.of("ssl=true", "timeout=30"), config.database().options()),

                // Verify replicas list
                () -> assertNotNull(config.replicas()),
                () -> assertEquals(2, config.replicas().size()),
                () -> assertEquals("replica1", config.replicas().get(0).host()),
                () -> assertEquals(5433, config.replicas().get(0).port()),
                () -> assertEquals(List.of("readOnly=true"), config.replicas().get(0).options()),

                // Verify replica2 was merged/appended
                () -> assertEquals("replica2", config.replicas().get(1).host()),
                () -> assertEquals(5434, config.replicas().get(1).port()),
                () -> assertEquals(List.of("readOnly=true"), config.replicas().get(1).options())
            );
        }
    }

    @Nested
    @DisplayName("Java Date and Time Types")
    class JavaDateTime {

        @Test
        @DisplayName("Should successfully parse Duration, Instant and LocalDate from YAML")
        void shouldSuccessfullyParseJavaTimeTypesFromYaml() {
            var yaml = "duration: \"PT0.5S\"\n" +
                "instant: \"2026-08-16T18:00:00Z\"\n" +
                "date: \"2026-08-16\"\n";

            var parser = new YamlParser(STANDARD_FEATURES);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
            var config = parser.convertTo(TestConfigWithTime.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(Duration.ofMillis(500), config.duration()),
                () -> assertEquals(Instant.parse("2026-08-16T18:00:00Z"), config.instant()),
                () -> assertEquals(LocalDate.of(2026, 8, 16), config.date())
            );
        }
    }

    @Nested
    @DisplayName("Placeholder Resolution")
    class PlaceholderResolution {

        @Test
        @DisplayName("Should resolve placeholders in scalar fields from environment variables")
        void shouldResolvePlaceholdersInScalarFields() {
            var yaml = "name: \"${APP_NAME}\"\nvalue: 42\n";
            var env = Map.of("APP_NAME", "ProductionApp");
            var placeholderResolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var parser = new YamlParser(builder -> {}, STANDARD_FEATURES, placeholderResolver);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
            var config = parser.convertTo(TestConfig.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("ProductionApp", config.name()),
                () -> assertEquals(42, config.value())
            );
        }

        @Test
        @DisplayName("Should resolve placeholders in list elements")
        void shouldResolvePlaceholdersInListElements() {
            var yaml = "items:\n  - \"${KAFKA_BROKER_1}\"\n  - \"${KAFKA_BROKER_2}\"\n";
            var env = Map.of(
                "KAFKA_BROKER_1", "kafka1:9092",
                "KAFKA_BROKER_2", "kafka2:9092"
            );
            var placeholderResolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var parser = new YamlParser(builder -> {}, STANDARD_FEATURES, placeholderResolver);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
            var config = parser.convertTo(TestConfigWithList.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(List.of("kafka1:9092", "kafka2:9092"), config.items())
            );
        }

        @Test
        @DisplayName("Should resolve placeholders in nested complex objects")
        void shouldResolvePlaceholdersInNestedComplexObjects() {
            var yaml = "env: \"${DEPLOY_ENV}\"\n" +
                "database:\n" +
                "  host: \"${DB_HOST}\"\n" +
                "  port: 5432\n" +
                "  options:\n" +
                "    - \"ssl=true\"\n" +
                "replicas: []\n";
            var env = Map.of(
                "DEPLOY_ENV", "prod",
                "DB_HOST", "postgres.prod.internal"
            );
            var placeholderResolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var parser = new YamlParser(builder -> {}, STANDARD_FEATURES, placeholderResolver);
            parser.parse(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
            var config = parser.convertTo(ComplexConfig.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals("prod", config.env()),
                () -> assertEquals("postgres.prod.internal", config.database().host()),
                () -> assertEquals(5432, config.database().port())
            );
        }

        @Test
        @DisplayName("Should fail fast with UnresolvedPropertyPlaceholderException when placeholder variable is missing in YAML")
        void shouldFailFastWhenPlaceholderMissing() {
            var yaml = "name: \"${UNDEFINED_SECRET}\"\nvalue: 1\n";
            var placeholderResolver = new PlaceholderResolver(new EnvVarResolver(key -> null));

            var parser = new YamlParser(builder -> {}, STANDARD_FEATURES, placeholderResolver);
            var stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            var exception = assertThrows(UnresolvedPropertyPlaceholderException.class, () -> parser.parse(stream));
            assertTrue(exception.getMessage().contains("UNDEFINED_SECRET"));
        }
    }

    static final class TestConfigWithTime {
        private final Duration duration;
        private final Instant instant;
        private final LocalDate date;

        @JsonCreator
        public TestConfigWithTime(
            @JsonProperty("duration") Duration duration,
            @JsonProperty("instant") Instant instant,
            @JsonProperty("date") LocalDate date
        ) {
            this.duration = Objects.requireNonNull(duration);
            this.instant = Objects.requireNonNull(instant);
            this.date = Objects.requireNonNull(date);
        }

        public Duration duration() {
            return duration;
        }

        public Instant instant() {
            return instant;
        }

        public LocalDate date() {
            return date;
        }
    }

    static final class FailingDeserializer extends JsonDeserializer<TestConfig> {

        static final String FAILING_DESERIALIZER_EXCEPTION_MESSAGE = "Deserializer rejected the configuration";

        @Override
        public TestConfig deserialize(JsonParser parser, DeserializationContext context) {
            throw new IllegalArgumentException(FAILING_DESERIALIZER_EXCEPTION_MESSAGE);
        }
    }
}
