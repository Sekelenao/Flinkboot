package io.github.sekelenao.api.exception.configuration;

import io.github.sekelenao.api.exception.FlinkbootException;

public class ConfigurationValidationException extends FlinkbootException {
    public ConfigurationValidationException(String message) {
        super(message);
    }
}
