package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class ExecutionEnvironmentConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    private final ExecutionConfiguration execution;

    @JsonCreator
    public ExecutionEnvironmentConfiguration(
        @JsonProperty("execution") ExecutionConfiguration execution
    ) {
        this.execution = execution;
    }

    public Optional<ExecutionConfiguration> execution() {
        return Optional.ofNullable(execution);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExecutionEnvironmentConfiguration)) {
            return false;
        }
        var o = (ExecutionEnvironmentConfiguration) other;
        return Objects.equals(execution, o.execution);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(execution);
    }

    @Override
    @Generated
    public String toString() {
        return "ExecutionEnvironmentConfiguration{" +
            "execution=" + execution +
            '}';
    }
}
