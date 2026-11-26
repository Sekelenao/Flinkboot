package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flinkboot.core.api.exception.parsing.YamlParsingException;
import com.flinkboot.core.internal.startup.EnvVarResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    @DisplayName("Scalar merging and case-insensitive overrides")
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
        @DisplayName("Should override case-colliding scalar in place while preserving key casing and order")
        void shouldOverrideCaseCollidingScalarInPlace() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, resolverWith(Map.of("NEW_NAME", "updated")));

            var first = FACTORY.objectNode()
                .put("firstKey", "a")
                .put("name", "original")
                .put("lastKey", "z");
            var second = FACTORY.objectNode()
                .put("NAME", "${new.name}");

            processor.apply(first);
            processor.apply(second);

            assertAll(
                () -> assertEquals(3, root.size()),
                () -> assertEquals(List.of("firstKey", "name", "lastKey"), fieldOrder(root)),
                () -> assertTrue(root.has("name")),
                () -> assertFalse(root.has("NAME")),
                () -> assertEquals("updated", root.get("name").asText())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException with existing key path when overriding case-colliding scalar is forbidden")
        void shouldThrowWhenCaseCollidingScalarOverrideIsForbidden() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            processor.apply(FACTORY.objectNode().put("name", "original"));
            var second = FACTORY.objectNode().put("NAME", "override");

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: name", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertFalse(root.has("NAME")),
                () -> assertEquals("original", root.get("name").asText())
            );
        }

        @Test
        @DisplayName("Should collapse multi-step overrides with varying case into original key without duplicates")
        void shouldCollapseMultiStepOverridesWithVaryingCase() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, emptyResolver());

            processor.apply(FACTORY.objectNode().put("name", "v1"));
            processor.apply(FACTORY.objectNode().put("Name", "v2"));
            processor.apply(FACTORY.objectNode().put("NAME", "v3"));

            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("name")),
                () -> assertFalse(root.has("Name")),
                () -> assertFalse(root.has("NAME")),
                () -> assertEquals("v3", root.get("name").asText())
            );
        }

        @Test
        @DisplayName("Should detect case-colliding duplicate keys within a single target object")
        void shouldDetectCaseCollidingKeysWithinSingleTarget() {
            var target = FACTORY.objectNode()
                .put("Name", "first")
                .put("NAME", "second");

            var overrideRoot = FACTORY.objectNode();
            var overrideProcessor = new MergeProcessor(overrideRoot, OVERRIDE_ONLY_FEATURES, emptyResolver());
            overrideProcessor.apply(target);

            var strictRoot = FACTORY.objectNode();
            var strictProcessor = new MergeProcessor(strictRoot, STRICT_FEATURES, emptyResolver());
            var exception = assertThrows(YamlParsingException.class, () -> strictProcessor.apply(target));

            assertAll(
                () -> assertEquals(1, overrideRoot.size()),
                () -> assertTrue(overrideRoot.has("Name")),
                () -> assertFalse(overrideRoot.has("name")),
                () -> assertFalse(overrideRoot.has("NAME")),
                () -> assertEquals("second", overrideRoot.get("Name").asText()),
                () -> assertEquals("Overriding an existing value is forbidden: Name", exception.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Nested object merging")
    class NestedObjectMerging {

        @Test
        @DisplayName("Should merge disjoint child keys under case-colliding parent when override is forbidden")
        void shouldMergeDisjointChildrenUnderCaseCollidingParent() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, resolverWith(Map.of("DB_PORT", "5432")));

            var first = objectWith("database", FACTORY.objectNode().put("host", "localhost"));
            var second = objectWith("DATABASE", FACTORY.objectNode().put("port", "${db.port}"));

            processor.apply(first);
            processor.apply(second);

            var database = root.get("database");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("database")),
                () -> assertFalse(root.has("DATABASE")),
                () -> assertEquals(2, database.size()),
                () -> assertEquals("localhost", database.get("host").asText()),
                () -> assertEquals("5432", database.get("port").asText())
            );
        }

        @Test
        @DisplayName("Should override case-colliding nested child under case-colliding parent when override is permitted")
        void shouldOverrideCaseCollidingNestedChild() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, resolverWith(Map.of("PROD_HOST", "db.prod")));

            var first = objectWith("database", FACTORY.objectNode().put("host", "localhost").put("port", 5432));
            var second = objectWith("DATABASE", FACTORY.objectNode().put("HOST", "${prod.host}"));

            processor.apply(first);
            processor.apply(second);

            var database = root.get("database");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertFalse(root.has("DATABASE")),
                () -> assertEquals(2, database.size()),
                () -> assertTrue(database.has("host")),
                () -> assertFalse(database.has("HOST")),
                () -> assertEquals("db.prod", database.get("host").asText()),
                () -> assertEquals(5432, database.get("port").asInt())
            );
        }

        @Test
        @DisplayName("Should report existing-key path when nested case-colliding child override is forbidden")
        void shouldReportExistingKeyPathForForbiddenNestedOverride() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            var first = objectWith("app",
                objectWith("Database",
                    objectWith("Pool", FACTORY.objectNode().put("MaxSize", 10))));
            var second = objectWith("APP",
                objectWith("database",
                    objectWith("POOL", FACTORY.objectNode().put("maxSize", 20))));

            processor.apply(first);

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertEquals("Overriding an existing value is forbidden: app.Database.Pool.MaxSize", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Array merging")
    class ArrayMerging {

        @Test
        @DisplayName("Should append elements with resolved placeholders into existing key when listMerging is enabled and permitOverride is false")
        void shouldMergeCaseCollidingArraysWhenListMergingEnabledAndOverrideForbidden() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, LIST_MERGING_ONLY_FEATURES, resolverWith(Map.of("EXTRA_ITEM", "c")));

            processor.apply(objectWith("items", arrayOf("a", "b")));
            processor.apply(objectWith("ITEMS", arrayOf("${extra.item}", "d")));

            var items = root.get("items");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("items")),
                () -> assertFalse(root.has("ITEMS")),
                () -> assertEquals(4, items.size()),
                () -> assertEquals("a", items.get(0).asText()),
                () -> assertEquals("b", items.get(1).asText()),
                () -> assertEquals("c", items.get(2).asText()),
                () -> assertEquals("d", items.get(3).asText())
            );
        }

        @Test
        @DisplayName("Should append elements into existing key when both listMerging and permitOverride are enabled")
        void shouldMergeCaseCollidingArraysWhenBothFlagsEnabled() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, ALL_FEATURES, emptyResolver());

            processor.apply(objectWith("items", arrayOf("a")));
            processor.apply(objectWith("Items", arrayOf("b")));

            var items = root.get("items");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("items")),
                () -> assertFalse(root.has("Items")),
                () -> assertEquals(2, items.size()),
                () -> assertEquals("a", items.get(0).asText()),
                () -> assertEquals("b", items.get(1).asText())
            );
        }

        @Test
        @DisplayName("Should replace array under existing key when listMerging is disabled and permitOverride is true")
        void shouldReplaceCaseCollidingArrayWhenListMergingDisabledAndOverridePermitted() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, resolverWith(Map.of("ITEM", "replaced")));

            processor.apply(objectWith("items", arrayOf("a", "b")));
            processor.apply(objectWith("ITEMS", arrayOf("${item}")));

            var items = root.get("items");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("items")),
                () -> assertFalse(root.has("ITEMS")),
                () -> assertEquals(1, items.size()),
                () -> assertEquals("replaced", items.get(0).asText())
            );
        }

        @Test
        @DisplayName("Should throw YamlParsingException when listMerging and permitOverride are both disabled")
        void shouldThrowWhenCaseCollidingArrayAndBothFlagsDisabled() {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            processor.apply(objectWith("items", arrayOf("a")));
            var second = objectWith("ITEMS", arrayOf("b"));

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: items", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertFalse(root.has("ITEMS")),
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
        @DisplayName("Should replace value under existing key on case-colliding type change when override is permitted")
        void shouldReplaceOnTypeChangeWhenOverridePermitted(String scenario, JsonNode initial, JsonNode replacement) {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, ALL_FEATURES, emptyResolver());

            processor.apply(objectWith("entry", initial.deepCopy()));
            processor.apply(objectWith("ENTRY", replacement.deepCopy()));

            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("entry")),
                () -> assertFalse(root.has("ENTRY")),
                () -> assertEquals(replacement, root.get("entry"))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("typeChangePairs")
        @DisplayName("Should throw YamlParsingException on case-colliding type change when override is forbidden")
        void shouldThrowOnTypeChangeWhenOverrideForbidden(String scenario, JsonNode initial, JsonNode replacement) {
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, LIST_MERGING_ONLY_FEATURES, emptyResolver());

            processor.apply(objectWith("entry", initial.deepCopy()));
            var second = objectWith("ENTRY", replacement.deepCopy());

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: entry", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertFalse(root.has("ENTRY")),
                () -> assertEquals(initial, root.get("entry"))
            );
        }
    }

    @Nested
    @DisplayName("Locale independence")
    class LocaleIndependence {

        private final Locale previousDefault = Locale.getDefault();

        @AfterEach
        void restoreLocale() {
            Locale.setDefault(previousDefault);
        }

        @Test
        @DisplayName("Should match id and ID case-insensitively under Turkish locale when override is permitted")
        void shouldOverrideIdUnderTurkishLocale() {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, OVERRIDE_ONLY_FEATURES, emptyResolver());

            processor.apply(FACTORY.objectNode().put("id", "first"));
            processor.apply(FACTORY.objectNode().put("ID", "second"));

            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("id")),
                () -> assertFalse(root.has("ID")),
                () -> assertEquals("second", root.get("id").asText())
            );
        }

        @Test
        @DisplayName("Should reject overriding id with ID under Turkish locale when override is forbidden")
        void shouldRejectIdOverrideUnderTurkishLocale() {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, STRICT_FEATURES, emptyResolver());

            processor.apply(FACTORY.objectNode().put("id", "first"));
            var second = FACTORY.objectNode().put("ID", "second");

            var exception = assertThrows(YamlParsingException.class, () -> processor.apply(second));

            assertAll(
                () -> assertEquals("Overriding an existing value is forbidden: id", exception.getMessage()),
                () -> assertEquals(1, root.size()),
                () -> assertFalse(root.has("ID"))
            );
        }

        @Test
        @DisplayName("Should merge items and ITEMS arrays under Turkish locale when listMerging is enabled")
        void shouldMergeItemsArrayUnderTurkishLocale() {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            var root = FACTORY.objectNode();
            var processor = new MergeProcessor(root, LIST_MERGING_ONLY_FEATURES, emptyResolver());

            processor.apply(objectWith("items", arrayOf("a")));
            processor.apply(objectWith("ITEMS", arrayOf("b")));

            var items = root.get("items");
            assertAll(
                () -> assertEquals(1, root.size()),
                () -> assertTrue(root.has("items")),
                () -> assertFalse(root.has("ITEMS")),
                () -> assertEquals(2, items.size()),
                () -> assertEquals("a", items.get(0).asText()),
                () -> assertEquals("b", items.get(1).asText())
            );
        }
    }
}
