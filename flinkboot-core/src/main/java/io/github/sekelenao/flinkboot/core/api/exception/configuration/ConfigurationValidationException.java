package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class ConfigurationValidationException extends FlinkbootException {

    private static final long serialVersionUID = 1L;
    public ConfigurationValidationException(String message) {
        super(message);
    }
}
