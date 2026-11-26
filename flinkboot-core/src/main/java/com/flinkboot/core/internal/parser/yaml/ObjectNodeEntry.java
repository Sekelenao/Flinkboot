package com.flinkboot.core.internal.parser.yaml;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

final class ObjectNodeEntry {

    private final String key;

    private final JsonNode value;

    ObjectNodeEntry(String key, JsonNode value) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
    }

    String key() {
        return key;
    }

    JsonNode value() {
        return value;
    }

}
