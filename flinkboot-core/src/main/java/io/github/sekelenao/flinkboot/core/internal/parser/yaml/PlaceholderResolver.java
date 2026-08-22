package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.UnresolvedPropertyPlaceholderException;
import io.github.sekelenao.flinkboot.core.internal.startup.EnvVarResolver;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderResolver {

    private static final Pattern PATTERN = Pattern.compile("(\\\\)?\\$\\{([^}]*)}");

    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_.-]+");

    private final EnvVarResolver envVarResolver;

    public PlaceholderResolver(EnvVarResolver envVarResolver) {
        this.envVarResolver = Objects.requireNonNull(envVarResolver);
    }

    public JsonNode resolve(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isTextual()) {
            return resolveTextNode((TextNode) node);
        }
        if (node.isArray()) {
            return resolveArrayNode((ArrayNode) node);
        }
        if (node.isObject()) {
            return resolveObjectNode((ObjectNode) node);
        }
        return node;
    }

    public TextNode resolveTextNode(TextNode node) {
        Objects.requireNonNull(node);
        return TextNode.valueOf(resolve(node.textValue()));
    }

    public ArrayNode resolveArrayNode(ArrayNode array) {
        Objects.requireNonNull(array);
        for (int i = 0; i < array.size(); i++) {
            array.set(i, resolve(array.get(i)));
        }
        return array;
    }

    public ObjectNode resolveObjectNode(ObjectNode object) {
        Objects.requireNonNull(object);
        for (var entry : object.properties()) {
            object.set(entry.getKey(), resolve(entry.getValue()));
        }
        return object;
    }

    public String resolve(String value) {
        Objects.requireNonNull(value);
        if(!value.contains("${")) {
            return value;
        }
        return PATTERN.matcher(value).replaceAll(matchResult -> {
            var isEscaped = "\\".equals(matchResult.group(1));
            var placeholderContent = matchResult.group(2);
            if (isEscaped) {
                return Matcher.quoteReplacement("${" + placeholderContent + "}");
            }
            if (placeholderContent.isBlank()) {
                throw new UnresolvedPropertyPlaceholderException("Empty or blank placeholder: ${" + placeholderContent + "}");
            }
            if (!VALID_NAME_PATTERN.matcher(placeholderContent).matches()) {
                throw new UnresolvedPropertyPlaceholderException("Malformed placeholder: ${" + placeholderContent + "}");
            }
            return envVarResolver.get(placeholderContent)
                .map(Matcher::quoteReplacement)
                .orElseThrow(() -> new UnresolvedPropertyPlaceholderException("Unresolved placeholder: " + placeholderContent));
        });
    }

}
