package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class CaseInsensitiveObjectNode {

    private final ObjectNode node;

    private final Map<String, String> keysByLower;

    private CaseInsensitiveObjectNode(ObjectNode node, Map<String, String> keysByLower) {
        this.node = node;
        this.keysByLower = keysByLower;
    }

    static CaseInsensitiveObjectNode from(ObjectNode node) {
        Objects.requireNonNull(node, "node must not be null");
        var keysByLower = new HashMap<String, String>();
        for (var entry : node.properties()) {
            var field = entry.getKey();
            var existing = keysByLower.putIfAbsent(field.toLowerCase(Locale.ROOT), field);
            if (existing != null) {
                throw new IllegalArgumentException(
                    "Duplicate case-insensitive key in ObjectNode: " + existing + " and " + field
                );
            }
        }
        return new CaseInsensitiveObjectNode(node, keysByLower);
    }

    Optional<ObjectNodeEntry> find(String key) {
        Objects.requireNonNull(key, "key must not be null");
        var existingKey = keysByLower.get(key.toLowerCase(Locale.ROOT));
        if (existingKey == null) {
            return Optional.empty();
        }
        return Optional.of(new ObjectNodeEntry(existingKey, node.get(existingKey)));
    }

    void put(String key, JsonNode value) {
        Objects.requireNonNull(key, "key must not be null");
        var normalized = key.toLowerCase(Locale.ROOT);
        var targetKey = keysByLower.getOrDefault(normalized, key);
        node.set(targetKey, value);
        keysByLower.putIfAbsent(normalized, targetKey);
    }

}
