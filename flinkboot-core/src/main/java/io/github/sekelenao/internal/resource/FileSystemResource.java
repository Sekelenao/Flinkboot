package io.github.sekelenao.internal.resource;

import io.github.sekelenao.api.exception.resource.ResourceAccessException;
import io.github.sekelenao.api.exception.resource.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Objects;

public class FileSystemResource implements Resource {

    private final String location;

    public FileSystemResource(String location) {
        this.location = Objects.requireNonNull(location);
    }

    @Override
    public InputStream inputStream() {
        var path = Paths.get(location);
        try {
            return Files.newInputStream(path);
        } catch (NoSuchFileException exception) {
            throw new ResourceNotFoundException(location);
        } catch (IOException exception) {
            throw new ResourceAccessException(location, exception);
        }
    }

}
