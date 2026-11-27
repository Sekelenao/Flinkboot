package com.flinkboot.fluss.internal;

import com.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import com.flinkboot.fluss.api.properties.source.FlussStartupMode;
import org.apache.fluss.client.initializer.OffsetsInitializer;

import java.util.Objects;

/**
 * Maps {@link FlussSourceProperties} startup configuration to Fluss {@link OffsetsInitializer} instances.
 */
public final class OffsetsInitializerMapper {

    private OffsetsInitializerMapper() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Resolves the {@link OffsetsInitializer} for the given {@link FlussSourceProperties}.
     *
     * @param config the Fluss source configuration properties
     * @return the resolved {@link OffsetsInitializer}
     * @throws NullPointerException  if {@code config} is {@code null}
     * @throws IllegalStateException if the startup mode is unsupported
     */
    public static OffsetsInitializer map(FlussSourceProperties config) {
        Objects.requireNonNull(config, "config must not be null");
        switch (config.startupMode()) {
            case EARLIEST:
            case LATEST:
            case FULL:
                return config.startupMode().offsetsInitializer().orElseThrow();
            case TIMESTAMP:
                return FlussStartupMode.fromTimestamp(config.startupTimestamp().orElseThrow());
            default:
                throw new IllegalStateException("Unsupported FlussStartupMode: " + config.startupMode());
        }
    }
}
