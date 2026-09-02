package io.github.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Standard reference implementation for Flinkboot Configuration Properties DTOs.
 */
public class StandardProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<@NotBlank String> bootstrapServers;

    @NotNull
    private final SampleMode mode;

    @PositiveOrZero
    private final Long timestamp;

    private final String description;

    private final Map<@NotNull String, @NotNull String> properties;

    @JsonCreator
    public StandardProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("mode") SampleMode mode,
        @JsonProperty("timestamp") Long timestamp,
        @JsonProperty("description") String description,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.mode = mode;
        this.timestamp = timestamp;
        this.description = description;
        this.properties = properties;
        validate();
    }

    private void validate() {
        if (mode == null) {
            return;
        }
        if (mode == SampleMode.CUSTOM) {
            if (timestamp == null) {
                throw new IllegalArgumentException("timestamp is required when mode is CUSTOM");
            }
        } else if (timestamp != null) {
            throw new IllegalArgumentException("timestamp must not be specified when mode is " + mode);
        }
    }

    public String name() {
        return name;
    }

    public List<String> bootstrapServers() {
        if (bootstrapServers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(bootstrapServers);
    }

    public SampleMode mode() {
        return mode;
    }

    public OptionalLong timestamp() {
        if (timestamp == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(timestamp);
    }

    public Optional<String> description() {
        if (description == null) {
            return Optional.empty();
        }
        return Optional.of(description);
    }

    public Map<String, String> properties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof StandardProperties)) {
            return false;
        }
        var o = (StandardProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(bootstrapServers, o.bootstrapServers)
            && mode == o.mode
            && Objects.equals(timestamp, o.timestamp)
            && Objects.equals(description, o.description)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, mode, timestamp, description, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "StandardProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", mode=" + mode +
            ", timestamp=" + timestamp +
            ", description='" + description + '\'' +
            ", properties=" + properties +
            '}';
    }

    public enum SampleMode {
        DEFAULT,
        CUSTOM
    }
}
