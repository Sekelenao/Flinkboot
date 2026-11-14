package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.internal.annotation.Generated;

import java.util.Objects;

/**
 * Immutable representation of a single cross-field property validation violation.
 */
public final class PropertyViolation {

    private final String property;
    private final String message;

    /**
     * Creates a new property violation.
     *
     * @param property the target property node name (must not be null)
     * @param message  the human-readable violation message (must not be null)
     */
    public PropertyViolation(String property, String message) {
        this.property = Objects.requireNonNull(property, "property must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    /**
     * Returns the target property node name.
     *
     * @return the property name
     */
    public String property() {
        return property;
    }

    /**
     * Returns the violation error message.
     *
     * @return the error message
     */
    public String message() {
        return message;
    }

    @Generated
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PropertyViolation)) {
            return false;
        }
        var o = (PropertyViolation) other;
        return Objects.equals(property, o.property) && Objects.equals(message, o.message);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(property, message);
    }

    @Generated
    @Override
    public String toString() {
        return "PropertyViolation{" +
            "property='" + property + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
