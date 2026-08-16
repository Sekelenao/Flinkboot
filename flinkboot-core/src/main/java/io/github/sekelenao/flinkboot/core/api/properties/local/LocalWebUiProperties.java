package io.github.sekelenao.flinkboot.core.api.properties.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Configuration properties for Flink local MiniCluster Web UI dashboard.
 */
public final class LocalWebUiProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Boolean enabled;

    @Positive
    private final Integer port;
    private final String bindAddress;

    /**
     * Creates a new {@code LocalWebUiProperties} instance.
     *
     * @param enabled     whether the local Web UI dashboard is enabled
     * @param port        the HTTP port to bind the Web UI to (e.g. 8081)
     * @param bindAddress the host interface to bind to (e.g. {@code "localhost"} or {@code "0.0.0.0"})
     */
    @JsonCreator
    public LocalWebUiProperties(
        @JsonProperty("enabled") Boolean enabled,
        @JsonProperty("port") Integer port,
        @JsonProperty("bind-address") String bindAddress
    ) {
        this.enabled = enabled;
        this.port = port;
        this.bindAddress = bindAddress;
    }

    /**
     * Returns whether the local Web UI is enabled.
     *
     * @return an {@link Optional} containing the enabled flag, or empty if not specified
     */
    public Optional<Boolean> enabled() {
        return Optional.ofNullable(enabled);
    }

    /**
     * Returns the HTTP port for the local Web UI.
     *
     * @return an {@link OptionalInt} containing the port number, or empty if not specified
     */
    public OptionalInt port() {
        if (port == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(port);
    }

    /**
     * Returns the host interface / bind address for the local Web UI.
     *
     * @return an {@link Optional} containing the bind address string, or empty if not specified
     */
    public Optional<String> bindAddress() {
        return Optional.ofNullable(bindAddress);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof LocalWebUiProperties)) {
            return false;
        }
        var o = (LocalWebUiProperties) other;
        return Objects.equals(enabled, o.enabled)
            && Objects.equals(port, o.port)
            && Objects.equals(bindAddress, o.bindAddress);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(enabled, port, bindAddress);
    }

    @Override
    @Generated
    public String toString() {
        return "LocalWebUiProperties{" +
            "enabled=" + enabled +
            ", port=" + port +
            ", bindAddress='" + bindAddress + '\'' +
            '}';
    }
}
