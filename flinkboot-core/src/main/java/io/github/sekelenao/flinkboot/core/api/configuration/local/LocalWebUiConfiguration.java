package io.github.sekelenao.flinkboot.core.api.configuration.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public final class LocalWebUiConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Boolean enabled;

    @Positive
    private final Integer port;
    private final String bindAddress;

    @JsonCreator
    public LocalWebUiConfiguration(
        @JsonProperty("enabled") Boolean enabled,
        @JsonProperty("port") Integer port,
        @JsonProperty("bind-address") String bindAddress
    ) {
        this.enabled = enabled;
        this.port = port;
        this.bindAddress = bindAddress;
    }

    public Optional<Boolean> enabled() {
        return Optional.ofNullable(enabled);
    }

    public OptionalInt port() {
        if (port == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(port);
    }

    public Optional<String> bindAddress() {
        return Optional.ofNullable(bindAddress);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof LocalWebUiConfiguration)) {
            return false;
        }
        var o = (LocalWebUiConfiguration) other;
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
        return "LocalWebUiConfiguration{" +
            "enabled=" + enabled +
            ", port=" + port +
            ", bindAddress='" + bindAddress + '\'' +
            '}';
    }
}
