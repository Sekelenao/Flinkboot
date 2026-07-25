package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidLocalWebUiConfigurationException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidLocalWebUiConfigurationException(String message) {
        super(message);
    }

    public InvalidLocalWebUiConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
