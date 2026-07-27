package io.github.sekelenao.flinkboot.core.api.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class JobProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @Valid
    private final ExecutionEnvironmentProperties environment;

    @JsonCreator
    public JobProperties(
        @JsonProperty("name") String name,
        @JsonProperty("environment") ExecutionEnvironmentProperties environment
    ) {
        this.name = Objects.requireNonNull(name);
        this.environment = environment;
    }

    public String name() {
        return name;
    }

    public Optional<ExecutionEnvironmentProperties> environment() {
        return Optional.ofNullable(environment);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof JobProperties)) {
            return false;
        }
        var o = (JobProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(environment, o.environment);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, environment);
    }

    @Override
    @Generated
    public String toString() {
        return "JobProperties{" +
            "name='" + name + '\'' +
            ", environment=" + environment +
            '}';
    }
}
