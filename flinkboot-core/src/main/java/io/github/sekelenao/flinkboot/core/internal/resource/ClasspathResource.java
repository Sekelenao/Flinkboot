package io.github.sekelenao.flinkboot.core.internal.resource;

import java.io.InputStream;
import java.util.Objects;

import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceNotFoundException;
import io.github.sekelenao.flinkboot.core.api.resource.Resource;

public class ClasspathResource implements Resource {

    private final String location;

    public ClasspathResource(String location) {
        this.location = Objects.requireNonNull(location);
    }

    @Override
    public InputStream inputStream() {
        var cleanPath = location;
        while (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }
        var classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathResource.class.getClassLoader();
        }
        var stream = classLoader.getResourceAsStream(cleanPath);
        if (stream == null) {
            throw new ResourceNotFoundException(location);
        }
        return stream;
    }

}
