package com.flinkboot.core.internal.validation;

import java.util.Objects;

public interface ConfigurationValidator extends AutoCloseable {

    static ConfigurationValidator from(ValidationFeatures features) {
        Objects.requireNonNull(features);
        if (features.disableValidation()) {
            return new NoOpConfigurationValidator();
        }
        return new JakartaConfigurationValidator(features.capacity());
    }

    <C> void validate(C configuration);

    @Override
    void close();
}
