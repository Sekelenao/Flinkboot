package io.github.example;

import java.util.Objects;

public record RecordDTO(String identifier, String metadata){

    public RecordDTO{
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(metadata);
    }

}
