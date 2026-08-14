package io.github.sekelenao.flinkboot.core.internal.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.ResourceNotFoundException;
import io.github.sekelenao.flinkboot.core.api.resource.Resource;

import java.io.InputStream;
import java.util.Objects;

public class ClasspathResource implements Resource {

    private final String location;

    public ClasspathResource(String location) {
        this.location = Objects.requireNonNull(location);
    }

    @Override
    public InputStream inputStream() {
        var cleanPath = location.replaceAll("^/+", "");
        var classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathResource.class.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        if (classLoader == null) {
            throw new ResourceNotFoundException(location);
        }
        var stream = classLoader.getResourceAsStream(cleanPath);
        if (stream == null) {
            throw new ResourceNotFoundException(location);
        }
        return stream;
    }

}
