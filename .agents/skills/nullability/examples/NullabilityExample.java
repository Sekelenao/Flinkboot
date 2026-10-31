package io.github.example;

import java.util.Objects;
import java.util.Optional;

public final class NullabilityExample {

    private final String identifier; // Mandatory

    private final String metadata;   // Optional

    public NullabilityExample(String identifier, String metadata) {
        this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
        this.metadata = metadata;
    }

    public String identifier() {
        return identifier;
    }

    public Optional<String> metadata() {
        return Optional.ofNullable(metadata);
    }
}
