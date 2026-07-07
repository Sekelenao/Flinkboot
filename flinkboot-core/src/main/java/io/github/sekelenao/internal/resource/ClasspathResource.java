package io.github.sekelenao.internal.resource;

import io.github.sekelenao.api.exception.resource.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ClasspathResource implements Resource {

    private final String location;

    public ClasspathResource(String location) {
        this.location = Objects.requireNonNull(location);
    }

    @Override
    public InputStream inputStream() {
        Objects.requireNonNull(location);
        var cleanPath = location.startsWith("/") ? location.substring(1) : location;
        var classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathResource.class.getClassLoader();
        }
        var stream = classLoader.getResourceAsStream(cleanPath);
        if(stream == null){
            throw new ResourceNotFoundException(location);
        }
        return stream;
    }

}
