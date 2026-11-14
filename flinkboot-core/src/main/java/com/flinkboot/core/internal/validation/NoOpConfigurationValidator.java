package com.flinkboot.core.internal.validation;

import java.util.Objects;

public final class NoOpConfigurationValidator implements ConfigurationValidator {

    @Override
    public <C> void validate(C configuration) {
        Objects.requireNonNull(configuration);
    }

    @Override
    public void close() {
        // No-op
    }
}
