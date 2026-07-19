package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

public final class JobConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @Valid
    @NotNull
    private final EnvironmentConfiguration environment;

    @JsonCreator
    public JobConfiguration(
        @JsonProperty("name") String name,
        @JsonProperty("environment") EnvironmentConfiguration environment
    ) {
        this.name = Objects.requireNonNull(name);
        this.environment = Objects.requireNonNull(environment);
    }

    public String name() {
        return name;
    }

    public EnvironmentConfiguration environment() {
        return environment;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof JobConfiguration)) {
            return false;
        }
        var otherJobConfiguration = (JobConfiguration) other;
        return Objects.equals(name, otherJobConfiguration.name)
            && Objects.equals(environment, otherJobConfiguration.environment);
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
