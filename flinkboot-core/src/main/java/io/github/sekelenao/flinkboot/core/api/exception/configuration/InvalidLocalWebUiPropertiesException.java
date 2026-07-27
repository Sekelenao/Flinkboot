package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public final class InvalidLocalWebUiPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidLocalWebUiPropertiesException(String message) {
        super(message);
    }

    public InvalidLocalWebUiPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
