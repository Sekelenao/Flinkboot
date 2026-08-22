package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.UnresolvedPropertyPlaceholderException;
import io.github.sekelenao.flinkboot.core.internal.startup.EnvVarResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PlaceholderResolver")
class PlaceholderResolverTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create instance when valid EnvVarResolver is provided")
        void shouldCreateInstanceWhenValidResolverProvided() {
            var envResolver = new EnvVarResolver(key -> "value");
            var resolver = new PlaceholderResolver(envResolver);
            assertNotNull(resolver);
        }

        @Test
        @DisplayName("Should throw NullPointerException when EnvVarResolver is null")
        void shouldThrowNullPointerExceptionWhenResolverIsNull() {
            assertThrows(NullPointerException.class, () -> new PlaceholderResolver(null));
        }
    }

    @Nested
    @DisplayName("Basic Resolution")
    class BasicResolution {

        @Test
        @DisplayName("Should return unchanged string when no placeholder is present")
        void shouldReturnUnchangedStringWhenNoPlaceholder() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> null));
            var result = resolver.resolve("plain-text-value");
            assertEquals("plain-text-value", result);
        }

        @Test
        @DisplayName("Should resolve single placeholder")
        void shouldResolveSinglePlaceholder() {
            var env = Map.of("KAFKA_HOST", "localhost:9092");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("${KAFKA_HOST}");
            assertEquals("localhost:9092", result);
        }

        @Test
        @DisplayName("Should resolve multiple placeholders in a single string")
        void shouldResolveMultiplePlaceholdersInSingleString() {
            var env = Map.of(
                "DB_HOST", "database.internal",
                "DB_PORT", "5432",
                "DB_NAME", "analytics"
            );
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}");
            assertEquals("jdbc:postgresql://database.internal:5432/analytics", result);
        }

        @Test
        @DisplayName("Should throw NullPointerException when value is null")
        void shouldThrowNullPointerExceptionWhenValueIsNull() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "test"));
            assertThrows(NullPointerException.class, () -> resolver.resolve((String) null));
        }
    }

    @Nested
    @DisplayName("Edge Cases & Special Characters (Regex Escaping)")
    class EdgeCasesAndSpecialCharacters {

        @Test
        @DisplayName("Should properly resolve value containing multiple dollar signs ($$) without regex crash")
        void shouldResolveValueWithDollarSigns() {
            var env = Map.of("DB_PASSWORD", "P@$$w0rd$1$secret");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("password: ${DB_PASSWORD}");
            assertEquals("password: P@$$w0rd$1$secret", result);
        }

        @Test
        @DisplayName("Should properly resolve value containing backslashes (\\) without escape crash")
        void shouldResolveValueWithBackslashes() {
            var env = Map.of("WINDOWS_PATH", "C:\\Program Files\\Flink\\logs\\app.log");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("path: ${WINDOWS_PATH}");
            assertEquals("path: C:\\Program Files\\Flink\\logs\\app.log", result);
        }

        @Test
        @DisplayName("Should properly resolve value containing URLs, colons, slashes, and query params")
        void shouldResolveValueWithUrlAndSpecialChars() {
            var env = Map.of("SERVICE_URL", "https://api.internal.net:8443/v1/stream?compress=true&retry=3");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("endpoint: ${SERVICE_URL}");
            assertEquals("endpoint: https://api.internal.net:8443/v1/stream?compress=true&retry=3", result);
        }

        @Test
        @DisplayName("Should properly resolve value containing JSON string brackets and quotes")
        void shouldResolveValueWithJsonContent() {
            var env = Map.of("JSON_PAYLOAD", "{\"key\": \"value\", \"count\": [1, 2, 3]}");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("payload: ${JSON_PAYLOAD}");
            assertEquals("payload: {\"key\": \"value\", \"count\": [1, 2, 3]}", result);
        }

        @Test
        @DisplayName("Should resolve extreme value with mixed backslashes, escape sequences, and regex backreferences ($0, $1, \\\\)")
        void shouldResolveExtremeSpecialCharacters() {
            var env = Map.of("EXTREME_SECRET", "$0\\$1\\\\$2\\t\\n$99$$");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("secret: ${EXTREME_SECRET}");
            assertEquals("secret: $0\\$1\\\\$2\\t\\n$99$$", result);
        }
    }

    @Nested
    @DisplayName("Case Normalization & Naming Conventions")
    class CaseNormalization {

        @Test
        @DisplayName("Should resolve placeholder written in kebab-case against SCREAMING_SNAKE_CASE env var")
        void shouldResolveKebabCasePlaceholder() {
            var env = Map.of("KAFKA_BOOTSTRAP_SERVERS", "broker1:9092");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("${kafka-bootstrap-servers}");
            assertEquals("broker1:9092", result);
        }

        @Test
        @DisplayName("Should resolve placeholder written in dot-notation against SCREAMING_SNAKE_CASE env var")
        void shouldResolveDotNotationPlaceholder() {
            var env = Map.of("KAFKA_BOOTSTRAP_SERVERS", "broker1:9092");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("${kafka.bootstrap.servers}");
            assertEquals("broker1:9092", result);
        }
    }

    @Nested
    @DisplayName("FailFastValidation")
    class FailFastValidation {

        @Test
        @DisplayName("Should throw UnresolvedPropertyPlaceholderException when environment variable is missing")
        void shouldThrowUnresolvedPropertyPlaceholderExceptionWhenMissing() {
            var env = Map.of("EXISTING_VAR", "present");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var exception = assertThrows(
                UnresolvedPropertyPlaceholderException.class,
                () -> resolver.resolve("Connecting to ${MISSING_VARIABLE_NAME}")
            );

            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("MISSING_VARIABLE_NAME"));
        }

        @Test
        @DisplayName("Should throw UnresolvedPropertyPlaceholderException on empty or blank placeholder")
        void shouldThrowExceptionOnEmptyOrBlankPlaceholder() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "value"));

            var ex1 = assertThrows(UnresolvedPropertyPlaceholderException.class, () -> resolver.resolve("${}"));
            assertTrue(ex1.getMessage().contains("Empty or blank placeholder"));

            var ex2 = assertThrows(UnresolvedPropertyPlaceholderException.class, () -> resolver.resolve("prefix-${   }-suffix"));
            assertTrue(ex2.getMessage().contains("Empty or blank placeholder"));
        }

        @Test
        @DisplayName("Should throw UnresolvedPropertyPlaceholderException on malformed placeholder syntax")
        void shouldThrowExceptionOnMalformedPlaceholderSyntax() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "value"));

            var ex1 = assertThrows(UnresolvedPropertyPlaceholderException.class, () -> resolver.resolve("${VAR:default}"));
            assertTrue(ex1.getMessage().contains("Malformed placeholder"));

            var ex2 = assertThrows(UnresolvedPropertyPlaceholderException.class, () -> resolver.resolve("${VAR#1}"));
            assertTrue(ex2.getMessage().contains("Malformed placeholder"));
        }

        @Test
        @DisplayName("Should leave non-placeholder syntax and empty strings untouched")
        void shouldLeaveNonPlaceholderSyntaxUntouched() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "value"));

            assertEquals("${UNCLOSED_VAR", resolver.resolve("${UNCLOSED_VAR"));
            assertEquals("$NOT_A_PLACEHOLDER", resolver.resolve("$NOT_A_PLACEHOLDER"));
            assertEquals("regular text with {brackets}", resolver.resolve("regular text with {brackets}"));
            assertEquals("", resolver.resolve(""));
            assertEquals("\"\"", resolver.resolve("\"\""));
        }
    }

    @Nested
    @DisplayName("Escaped Placeholders")
    class EscapedPlaceholders {

        @Test
        @DisplayName("Should preserve escaped placeholder as literal string without querying environment")
        void shouldPreserveEscapedPlaceholder() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> {
                throw new AssertionError("Should not query env for escaped placeholder: " + key);
            }));

            var result = resolver.resolve("\\${literal_placeholder}");
            assertEquals("${literal_placeholder}", result);
        }

        @Test
        @DisplayName("Should preserve escaped empty placeholder as literal string")
        void shouldPreserveEscapedEmptyPlaceholder() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> null));
            var result = resolver.resolve("\\${}");
            assertEquals("${}", result);
        }

        @Test
        @DisplayName("Should resolve mixed escaped and unescaped placeholders in the same string")
        void shouldResolveMixedEscapedAndUnescapedPlaceholders() {
            var env = Map.of("DEPLOY_ENV", "prod");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var result = resolver.resolve("path: /data/year=\\${year}/env=${DEPLOY_ENV}");
            assertEquals("path: /data/year=${year}/env=prod", result);
        }

        @Test
        @DisplayName("Should resolve multiple escaped placeholders in a single string")
        void shouldResolveMultipleEscapedPlaceholders() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> null));
            var result = resolver.resolve("s3://bucket/\\${year}/\\${month}/\\${day}");
            assertEquals("s3://bucket/${year}/${month}/${day}", result);
        }
    }

    @Nested
    @DisplayName("Jackson Node Resolution")
    class JacksonNodeResolution {

        private final ObjectMapper mapper = new ObjectMapper();

        @Test
        @DisplayName("Should return null or null node when resolving null JsonNode")
        void shouldHandleNullJsonNode() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "val"));

            assertNull(resolver.resolve((JsonNode) null));
            assertSame(NullNode.getInstance(), resolver.resolve((JsonNode) NullNode.getInstance()));
        }

        @Test
        @DisplayName("Should return non-textual scalar node untouched")
        void shouldReturnNonTextualScalarUntouched() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "val"));
            var intNode = IntNode.valueOf(42);

            var result = resolver.resolve(intNode);
            assertSame(intNode, result);
        }

        @Test
        @DisplayName("Should resolve TextNode")
        void shouldResolveTextNode() {
            var env = Map.of("HOST", "localhost");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));
            var textNode = TextNode.valueOf("server: ${HOST}");

            var result = resolver.resolveTextNode(textNode);
            assertEquals("server: localhost", result.textValue());
        }

        @Test
        @DisplayName("Should throw NullPointerException when resolveTextNode receives null")
        void shouldThrowExceptionWhenTextNodeIsNull() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "val"));
            assertThrows(NullPointerException.class, () -> resolver.resolveTextNode(null));
        }

        @Test
        @DisplayName("Should resolve ArrayNode elements recursively")
        void shouldResolveArrayNode() {
            var env = Map.of(
                "BROKER_1", "b1:9092",
                "BROKER_2", "b2:9092"
            );
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));
            var array = mapper.createArrayNode();
            array.add("${BROKER_1}");
            array.add("${BROKER_2}");
            array.add(100);

            var result = resolver.resolveArrayNode(array);
            assertEquals("b1:9092", result.get(0).textValue());
            assertEquals("b2:9092", result.get(1).textValue());
            assertEquals(100, result.get(2).intValue());
        }

        @Test
        @DisplayName("Should throw NullPointerException when resolveArrayNode receives null")
        void shouldThrowExceptionWhenArrayNodeIsNull() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "val"));
            assertThrows(NullPointerException.class, () -> resolver.resolveArrayNode(null));
        }

        @Test
        @DisplayName("Should resolve ObjectNode properties recursively")
        void shouldResolveObjectNode() {
            var env = Map.of(
                "DB_USER", "postgres",
                "DB_PASS", "s3cr3t"
            );
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));
            var object = mapper.createObjectNode();
            object.put("user", "${DB_USER}");
            object.put("pass", "${DB_PASS}");
            object.put("port", 5432);

            var result = resolver.resolveObjectNode(object);
            assertEquals("postgres", result.get("user").textValue());
            assertEquals("s3cr3t", result.get("pass").textValue());
            assertEquals(5432, result.get("port").intValue());
        }

        @Test
        @DisplayName("Should throw NullPointerException when resolveObjectNode receives null")
        void shouldThrowExceptionWhenObjectNodeIsNull() {
            var resolver = new PlaceholderResolver(new EnvVarResolver(key -> "val"));
            assertThrows(NullPointerException.class, () -> resolver.resolveObjectNode(null));
        }

        @Test
        @DisplayName("Should polymorphically resolve through resolve(JsonNode)")
        void shouldPolymorphicallyResolveJsonNode() {
            var env = Map.of("VAL", "hello");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var textResult = resolver.resolve((JsonNode) TextNode.valueOf("${VAL}"));
            assertEquals("hello", textResult.textValue());

            var array = mapper.createArrayNode();
            array.add("${VAL}");
            var arrayResult = resolver.resolve((JsonNode) array);
            assertEquals("hello", arrayResult.get(0).textValue());

            var object = mapper.createObjectNode();
            object.put("k", "${VAL}");
            var objectResult = resolver.resolve((JsonNode) object);
            assertEquals("hello", objectResult.get("k").textValue());
        }

        @Test
        @DisplayName("Should resolve multidimensional arrays (arrays of arrays / matrices)")
        void shouldResolveMultidimensionalArrays() {
            var env = Map.of(
                "A", "valA",
                "B", "valB",
                "C", "valC",
                "D", "valD"
            );
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            var matrix = mapper.createArrayNode();
            var row1 = mapper.createArrayNode();
            row1.add("${A}");
            row1.add("${B}");
            var row2 = mapper.createArrayNode();
            row2.add("${C}");
            row2.add("${D}");
            matrix.add(row1);
            matrix.add(row2);

            var result = resolver.resolveArrayNode(matrix);

            assertEquals("valA", result.get(0).get(0).textValue());
            assertEquals("valB", result.get(0).get(1).textValue());
            assertEquals("valC", result.get(1).get(0).textValue());
            assertEquals("valD", result.get(1).get(1).textValue());
        }

        @Test
        @DisplayName("Should resolve deeply nested structures of arbitrary depth")
        void shouldResolveDeeplyNestedStructures() {
            var env = Map.of("DEEP_SECRET", "super-secret");
            var resolver = new PlaceholderResolver(new EnvVarResolver(env::get));

            // root -> list -> obj -> list -> matrix -> obj -> field
            var root = mapper.createObjectNode();
            var list = mapper.createArrayNode();
            var obj1 = mapper.createObjectNode();
            var innerList = mapper.createArrayNode();
            var deepObj = mapper.createObjectNode();
            deepObj.put("secret", "${DEEP_SECRET}");
            innerList.add(deepObj);
            obj1.set("inner", innerList);
            list.add(obj1);
            root.set("data", list);

            var result = (ObjectNode) resolver.resolve((JsonNode) root);
            var extractedSecret = result.get("data").get(0).get("inner").get(0).get("secret").textValue();

            assertEquals("super-secret", extractedSecret);
        }
    }
}
