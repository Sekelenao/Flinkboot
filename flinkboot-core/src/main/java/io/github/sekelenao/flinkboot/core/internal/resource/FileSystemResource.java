package io.github.sekelenao.flinkboot.core.internal.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceAccessException;
import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceNotFoundException;
import io.github.sekelenao.flinkboot.core.api.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileSystemResource implements Resource {

    private final String location;

    public FileSystemResource(String location) {
        this.location = Objects.requireNonNull(location);
    }

    private Path resolvePath() {
        var cleanLocation = location;
        if (cleanLocation.matches("^/+[a-zA-Z]:.*")) {
            cleanLocation = cleanLocation.replaceAll("^/+", "");
        } else if (cleanLocation.startsWith("//")) {
            cleanLocation = "/" + cleanLocation.replaceAll("^/+", "");
        }
        try {
            return Paths.get(cleanLocation);
        } catch (InvalidPathException exception) {
            throw new ResourceAccessException(location, exception);
        }
    }

    @Override
    public InputStream inputStream() {
        var path = resolvePath();
        if (Files.isDirectory(path)) {
            throw new ResourceAccessException(location + " is a directory, not a readable resource");
        }
        try {
            return Files.newInputStream(path);
        } catch (NoSuchFileException exception) {
            throw new ResourceNotFoundException(location);
        } catch (IOException exception) {
            throw new ResourceAccessException(location, exception);
        }
    }

}
