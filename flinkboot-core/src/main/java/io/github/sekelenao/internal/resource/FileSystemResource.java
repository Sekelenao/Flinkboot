package io.github.sekelenao.internal.resource;

import io.github.sekelenao.api.exception.resource.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class FileSystemResource implements Resource {

    private final String location;

    public FileSystemResource(String location) {
        this.location = Objects.requireNonNull(location);
    }

    @Override
    public InputStream inputStream() throws IOException {
        var path = Paths.get(location);
        if(Files.notExists(path)){
            throw new ResourceNotFoundException(location);
        }
        return Files.newInputStream(path);
    }

}
