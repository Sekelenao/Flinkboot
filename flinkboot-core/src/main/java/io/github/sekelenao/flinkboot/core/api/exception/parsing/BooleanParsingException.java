package io.github.sekelenao.flinkboot.core.api.exception.parsing;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class BooleanParsingException extends FlinkbootException {

    private static final long serialVersionUID = 1L;
    public BooleanParsingException(String message) {
        super(message);
    }
}
