package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class JobConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @Valid
    private final ExecutionEnvironmentConfiguration environment;

    @JsonCreator
    public JobConfiguration(
        @JsonProperty("name") String name,
        @JsonProperty("environment") ExecutionEnvironmentConfiguration environment
    ) {
        this.name = Objects.requireNonNull(name);
        this.environment = environment;
    }

    public String name() {
        return name;
    }

    public Optional<ExecutionEnvironmentConfiguration> environment() {
        return Optional.ofNullable(environment);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof JobConfiguration)) {
            return false;
        }
        var o = (JobConfiguration) other;
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
        return "JobConfiguration{" +
            "name='" + name + '\'' +
            ", environment=" + environment +
            '}';
    }
}
