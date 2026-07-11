package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class ConfigurationValidationException extends FlinkbootException {
    public ConfigurationValidationException(String message) {
        super(message);
    }
}
