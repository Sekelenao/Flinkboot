package io.github.sekelenao.flinkboot.core.api.exception;

public class FlinkbootException extends RuntimeException {

    public FlinkbootException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlinkbootException(String message) {
        super(message);
    }

}
