package io.github.example;

import com.flinkboot.core.internal.annotation.Generated;

import java.util.Objects;

public final class ClassDTO {

    private final String identifier;

    public ClassDTO(String identifier) {
        this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
    }

    public String identifier() {
        return identifier;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ClassDTO)) {
            return false;
        }
        var o = (ClassDTO) other;
        return Objects.equals(identifier, o.identifier);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    @Generated
    public String toString() {
        return "ClassDTO{" +
            "identifier='" + identifier + '\'' +
            '}';
    }
}
