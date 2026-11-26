package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CaseInsensitiveObjectNode")
class CaseInsensitiveObjectNodeTest {

    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    @Nested
    @DisplayName("Null validation")
    class NullValidation {

        @Test
        @DisplayName("Rejects null ObjectNode in constructor")
        void shouldRejectNullNode() {
            assertThrows(NullPointerException.class, () -> new CaseInsensitiveObjectNode(null));
        }

        @Test
        @DisplayName("Rejects null key in find and put")
        void shouldRejectNullKey() {
            var node = new CaseInsensitiveObjectNode(FACTORY.objectNode());

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> node.find(null)),
                () -> assertThrows(NullPointerException.class, () -> node.put(null, TextNode.valueOf("v")))
            );
        }

        @Test
        @DisplayName("Rejects null key or value in ObjectNodeEntry")
        void shouldRejectNullEntryArguments() {
            assertAll(
                () -> assertThrows(NullPointerException.class, () -> new ObjectNodeEntry(null, TextNode.valueOf("v"))),
                () -> assertThrows(NullPointerException.class, () -> new ObjectNodeEntry("key", null))
            );
        }

    }

    @Nested
    @DisplayName("Find")
    class Find {

        @Test
        @DisplayName("Returns empty when key is absent")
        void shouldReturnEmptyWhenKeyIsAbsent() {
            ObjectNode backing = FACTORY.objectNode();
            backing.put("name", "job");
            var node = new CaseInsensitiveObjectNode(backing);

            assertTrue(node.find("other").isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"Name", "name", "NAME", "nAmE"})
        @DisplayName("Finds pre-existing key regardless of case and returns canonical key and value")
        void shouldFindPreExistingKeyIgnoringCase(String lookupKey) {
            ObjectNode backing = FACTORY.objectNode();
            backing.put("Name", "flink-job");
            var node = new CaseInsensitiveObjectNode(backing);

            var entry = node.find(lookupKey);

            assertAll(
                () -> assertTrue(entry.isPresent()),
                () -> assertEquals("Name", entry.orElseThrow().key()),
                () -> assertEquals("flink-job", entry.orElseThrow().value().asText())
            );
        }

    }

    @Nested
    @DisplayName("Put")
    class Put {

        @Test
        @DisplayName("Inserts new mixed-case key and indexes it for subsequent case-insensitive lookups")
        void shouldInsertAndIndexNewMixedCaseKey() {
            ObjectNode backing = FACTORY.objectNode();
            var node = new CaseInsensitiveObjectNode(backing);

            node.put("JobName", TextNode.valueOf("alpha"));

            var foundLower = node.find("jobname");
            var foundUpper = node.find("JOBNAME");
            assertAll(
                () -> assertEquals(1, backing.size()),
                () -> assertEquals("alpha", backing.get("JobName").asText()),
                () -> assertTrue(foundLower.isPresent()),
                () -> assertEquals("JobName", foundLower.orElseThrow().key()),
                () -> assertEquals("alpha", foundLower.orElseThrow().value().asText()),
                () -> assertTrue(foundUpper.isPresent()),
                () -> assertEquals("JobName", foundUpper.orElseThrow().key())
            );
        }

        @Test
        @DisplayName("Replaces pre-existing key in place preserving canonical key and field order")
        void shouldReplacePreExistingKeyInPlacePreservingOrder() {
            ObjectNode backing = FACTORY.objectNode();
            backing.put("First", "1");
            backing.put("Name", "initial");
            backing.put("Last", "3");
            var node = new CaseInsensitiveObjectNode(backing);

            node.put("NAME", TextNode.valueOf("updated"));

            List<String> keys = new ArrayList<>();
            backing.fieldNames().forEachRemaining(keys::add);
            assertAll(
                () -> assertEquals(List.of("First", "Name", "Last"), keys),
                () -> assertEquals("updated", backing.get("Name").asText()),
                () -> assertFalse(backing.has("NAME")),
                () -> assertEquals("updated", node.find("name").orElseThrow().value().asText())
            );
        }

        @Test
        @DisplayName("Replaces newly inserted key across varying case without creating duplicates")
        void shouldReplaceNewlyInsertedKeyAcrossVaryingCase() {
            ObjectNode backing = FACTORY.objectNode();
            var node = new CaseInsensitiveObjectNode(backing);

            node.put("Name", TextNode.valueOf("first"));
            node.put("name", TextNode.valueOf("second"));
            node.put("NAME", TextNode.valueOf("third"));

            assertAll(
                () -> assertEquals(1, backing.size()),
                () -> assertTrue(backing.has("Name")),
                () -> assertFalse(backing.has("name")),
                () -> assertFalse(backing.has("NAME")),
                () -> assertEquals("third", backing.get("Name").asText())
            );
        }

    }

    @Nested
    @DisplayName("Locale independence")
    class LocaleIndependence {

        private final Locale originalLocale = Locale.getDefault();

        @AfterEach
        void restoreLocale() {
            Locale.setDefault(originalLocale);
        }

        @Test
        @DisplayName("Matches and updates I/i keys identically under Turkish locale")
        void shouldUseRootLocaleUnderTurkishDefault() {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            ObjectNode backing = FACTORY.objectNode();
            backing.put("items", "first");
            var node = new CaseInsensitiveObjectNode(backing);

            var found = node.find("ITEMS");
            node.put("ITEMS", TextNode.valueOf("second"));

            assertAll(
                () -> assertTrue(found.isPresent()),
                () -> assertEquals("items", found.orElseThrow().key()),
                () -> assertEquals(1, backing.size()),
                () -> assertEquals("second", backing.get("items").asText()),
                () -> assertFalse(backing.has("ITEMS"))
            );
        }

    }

}
