package io.github.sekelenao.flinkboot.core.api.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Root configuration properties describing a Flink job, including its name and execution environment.
 */
public final class JobProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @Valid
    private final ExecutionEnvironmentProperties environment;

    /**
     * Creates a new {@code JobProperties} instance.
     *
     * @param name        the unique name of the Flink job
     * @param environment the execution environment configuration
     */
    @JsonCreator
    public JobProperties(
        @JsonProperty("name") String name,
        @JsonProperty("environment") ExecutionEnvironmentProperties environment
    ) {
        this.name = name;
        this.environment = environment;
    }

    /**
     * Returns the name of the Flink job.
     *
     * @return the job name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the optional execution environment configuration.
     *
     * @return an {@link Optional} containing the execution environment configuration, or empty if not configured
     */
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
