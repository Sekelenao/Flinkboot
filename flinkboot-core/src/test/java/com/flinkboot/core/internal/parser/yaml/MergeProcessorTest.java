package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flinkboot.core.api.exception.parsing.YamlParsingException;
import com.flinkboot.core.internal.startup.EnvVarResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MergeProcessor")
class MergeProcessorTest {

    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    private static final ParserFeatures STRICT_FEATURES = ParserFeatures.builder()
        .permitOverride(false)
        .listMerging(false)
        .build();

    private static final ParserFeatures OVERRIDE_ONLY_FEATURES = ParserFeatures.builder()
        .permitOverride(true)
        .listMerging(false)
        .build();

    private static final ParserFeatures LIST_MERGING_ONLY_FEATURES = ParserFeatures.builder()
        .permitOverride(false)
        .listMerging(true)
        .build();

    private static final ParserFeatures ALL_FEATURES = ParserFeatures.builder()
        .permitOverride(true)
        .listMerging(true)
        .build();

    private static PlaceholderResolver resolverWith(Map<String, String> env) {
        return new PlaceholderResolver(new EnvVarResolver(env::get));
    }

    private static PlaceholderResolver emptyResolver() {
        return resolverWith(Map.of());
    }

    private static List<String> fieldOrder(ObjectNode node) {
        var names = new ArrayList<String>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static ObjectNode objectWith(String key, JsonNode value) {
        var node = FACTORY.objectNode();
        node.set(key, value);
        return node;
    }

    private static ArrayNode arrayOf(String... values) {
        var array = FACTORY.arrayNode();
        for (var value : values) {
            array.add(value);
        }
        return array;
    }

    @Nested
    @DisplayName("Null validation")
    class NullValidation {

        @Test
        @DisplayName("Should reject null constructor arguments")
        void shouldRejectNullConstructorArguments() {
            var root = FACTORY.objectNode();
            var resolver = emptyResolver();

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> new MergeProcessor(null, STRICT_FEATURES, resolver)),
                () -> assertThrows(NullPointerException.class, () -> new MergeProcessor(root, null, resolver)),
                () -> assertThrows(NullPointerException.class, () -> new MergeProcessor(root, STRICT_FEATURES, null))
            );
        }

        @Test
        @DisplayName("Should reject null target in apply")
        void shouldRejectNullTarget() {
            var processor = new MergeProcessor(FACTORY.objectNode(), STRICT_FEATURES, emptyResolver());

            assertThrows(NullPointerException.class, () -> processor.apply(null));
        }
    }

    @Nested
    @DisplayName("Scalar merging and case sensitivity")
    class ScalarMerging {

        @Test
        @DisplayName("Should insert new keys and resolve placeholders")
        void shouldInsertNewKeysAndResolvePlaceholders() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, resolverWith(Map.of("APP_NAME", "flink-job")));
            var target = FACTORY.objectNode()
                .put("name", "${app.name}")
                .put("parallelism", 4);

            processor.apply(target);

            assertAll(
                () -> assertEquals(2, root.size()),
                () -> assertEquals("flink-job", root.get("name").asText()),
                () -> assertEquals(4, root.get("parallelism").asInt())
            );
        }

        @Test
        @DisplayName("Should override matching scalar in place while preserving key order")
        void shouldOverrideMatchingScalarInPlace() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, resolverWith(Map.of("NEW_NAME", "updated")));

            var first = FACTORY.objectNode()
                .put("firstKey", "a")
                .put("name", "original")
                .put("lastKey", "z");
            var second = FACTORY.objectNode()
                .put("name", "${new.name}");

            processor.apply(first);
            processor.apply(second);

            assertAll(
                () -> assertEquals(3, root.size()),
                () -> assertEquals(List.of("firstKey", "name", "lastKey"), fieldOrder(root)),
                () -> assertEquals("updated", root.get("name").asText())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when overriding an existing scalar is forbidden")
        void shouldThrowWhenScalarOverrideIsForbidden() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            processor.apply(FACTORY.objectNode().put("name", "original"));
            var second = FACTORY.objectNode().put("name", "override");

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: name", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertEquals("original", root.get("name").asText())
            );
        }

        @Test
        @DisplayName("Should preserve keys differing only in case within a single target and across apply calls")
        void shouldPreserveKeysDifferingOnlyInCase() {
            var strictRoot = FACTORY.objectNode();
            var strictProcessor = new MergeProcessor(strictRoot, STRICT_FEATURES, resolverWith(Map.of("UPPER_VAL", "B")));
            strictProcessor.apply(FACTORY.objectNode().put("foo", "a").put("FOO", "${upper.val}"));

            var overrideRoot = FACTORY.objectNode();
            var overrideProcessor = new MergeProcessor(overrideRoot, OVERRIDE_ONLY_FEATURES, emptyResolver());
            overrideProcessor.apply(FACTORY.objectNode().put("foo", "a"));
            overrideProcessor.apply(FACTORY.objectNode().put("FOO", "b"));

            assertAll(
                () -> assertEquals(2, strictRoot.size()),
                () -> assertEquals("a", strictRoot.get("foo").asText()),
                () -> assertEquals("B", strictRoot.get("FOO").asText()),
                () -> assertEquals(2, overrideRoot.size()),
                () -> assertEquals("a", overrideRoot.get("foo").asText()),
                () -> assertEquals("b", overrideRoot.get("FOO").asText())
            );
        }
    }

    @Nested
    @DisplayName("Nested object merging")
    class NestedObjectMerging {

        @Test
        @DisplayName("Should merge disjoint child keys under matching parent when override is forbidden")
        void shouldMergeDisjointChildrenUnderMatchingParent() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, resolverWith(Map.of("DB_PORT", "5432")));

            var first = objectWith("database", FACTORY.objectNode().put("host", "localhost"));
            var second = objectWith("database", FACTORY.objectNode().put("port", "${db.port}"));

            processor.apply(first);
            processor.apply(second);

            var database = root.get("database");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertEquals(2, database.size()),
                () -> assertEquals("localhost", database.get("host").asText()),
                () -> assertEquals("5432", database.get("port").asText())
            );
        }

        @Test
        @DisplayName("Should preserve case-distinct keys inside nested map objects")
        void shouldPreserveCaseDistinctKeysInsideNestedObjects() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, resolverWith(Map.of("VAL_UPPER", "prod")));

            var first = objectWith("properties", FACTORY.objectNode().put("env", "dev").put("ENV", "${val.upper}"));
            var second = objectWith("properties", FACTORY.objectNode().put("Env", "staging"));

            processor.apply(first);
            processor.apply(second);

            var properties = root.get("properties");
            assertAll(
                () -> assertEquals(3, properties.size()),
                () -> assertEquals("dev", properties.get("env").asText()),
                () -> assertEquals("prod", properties.get("ENV").asText()),
                () -> assertEquals("staging", properties.get("Env").asText())
            );
        }

        @Test
        @DisplayName("Should report full nested path when nested override is forbidden")
        void shouldReportFullPathForForbiddenNestedOverride() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            var first = objectWith("app",
                objectWith("database",
                    objectWith("pool", FACTORY.objectNode().put("max-size", 10))));
            var second = objectWith("app",
                objectWith("database",
                    objectWith("pool", FACTORY.objectNode().put("max-size", 20))));

            processor.apply(first);

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertEquals("Overriding an existing value is forbidden: app.database.pool.max-size", exception.getMessage());
        }

        @Test
        @DisplayName("Should resolve placeholders inside newly inserted and overriding nested objects")
        void shouldResolvePlaceholdersInInsertedAndOverridingNestedObjects() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, resolverWith(Map.of("DB_HOST", "db.prod")));
            processor.apply(FACTORY.objectNode().put("database", "legacy"));

            var target = objectWith("database", FACTORY.objectNode().put("host", "${db.host}"));
            processor.apply(target);

            var database = root.get("database");
            assertAll(
                () -> assertTrue(database.isObject()),
                () -> assertEquals(1, database.size()),
                () -> assertEquals("db.prod", database.get("host").asText())
            );
        }
    }

    @Nested
    @DisplayName("Array merging")
    class ArrayMerging {

        @Test
        @DisplayName("Should append elements with resolved placeholders when listMerging is enabled and permitOverride is false")
        void shouldMergeArraysWhenListMergingEnabledAndOverrideForbidden() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, LIST_MERGING_ONLY_FEATURES, resolverWith(Map.of("EXTRA_ITEM", "c")));

            processor.apply(objectWith("items", arrayOf("a", "b")));
            processor.apply(objectWith("items", arrayOf("${extra.item}", "d")));

            var items = root.get("items");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertEquals(4, items.size()),
                () -> assertEquals("a", items.get(0).asText()),
                () -> assertEquals("b", items.get(1).asText()),
                () -> assertEquals("c", items.get(2).asText()),
                () -> assertEquals("d", items.get(3).asText())
            );
        }

        @Test
        @DisplayName("Should replace array when listMerging is disabled and permitOverride is true")
        void shouldReplaceArrayWhenListMergingDisabledAndOverridePermitted() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, resolverWith(Map.of("ITEM", "replaced")));

            processor.apply(objectWith("items", arrayOf("a", "b")));
            processor.apply(objectWith("items", arrayOf("${item}")));

            var items = root.get("items");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertEquals(1, items.size()),
                () -> assertEquals("replaced", items.get(0).asText())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when listMerging and permitOverride are both disabled")
        void shouldThrowWhenArrayOverrideAndListMergingAreBothDisabled() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            processor.apply(objectWith("items", arrayOf("a")));
            var second = objectWith("items", arrayOf("b"));

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: items", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertEquals(1, root.get("items").size())
            );
        }
    }

    @Nested
    @DisplayName("Type-change collisions")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class TypeChangeCollisions {

        Stream<Arguments> typeChangePairs() {
            return Stream.of(
                Arguments.of("scalar to object", TextNode.valueOf("v"), FACTORY.objectNode().put("k", "v")),
                Arguments.of("object to scalar", FACTORY.objectNode().put("k", "v"), TextNode.valueOf("v")),
                Arguments.of("scalar to array", TextNode.valueOf("v"), arrayOf("a")),
                Arguments.of("array to scalar", arrayOf("a"), TextNode.valueOf("v")),
                Arguments.of("object to array", FACTORY.objectNode().put("k", "v"), arrayOf("a")),
                Arguments.of("array to object", arrayOf("a"), FACTORY.objectNode().put("k", "v"))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("typeChangePairs")
        @DisplayName("Should replace value on type change when override is permitted")
        void shouldReplaceOnTypeChangeWhenOverridePermitted(String scenario, JsonNode initial, JsonNode replacement) {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, ALL_FEATURES, emptyResolver());

            processor.apply(objectWith("entry", initial.deepCopy()));
            processor.apply(objectWith("entry", replacement.deepCopy()));

            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertEquals(replacement, root.get("entry"))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("typeChangePairs")
        @DisplayName("Should throw YamlParsingException on type change when override is forbidden")
        void shouldThrowOnTypeChangeWhenOverrideForbidden(String scenario, JsonNode initial, JsonNode replacement) {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, LIST_MERGING_ONLY_FEATURES, emptyResolver());

            processor.apply(objectWith("entry", initial.deepCopy()));
            var second = objectWith("entry", replacement.deepCopy());

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: entry", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertEquals(initial, root.get("entry"))
            );
        }
    }
}
