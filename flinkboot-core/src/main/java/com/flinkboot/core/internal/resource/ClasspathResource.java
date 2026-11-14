package com.flinkboot.core.internal.resource;

import com.flinkboot.core.api.exception.resource.ResourceNotFoundException;
import com.flinkboot.core.api.resource.Resource;

import java.io.InputStream;
import java.util.Objects;

public final class ClasspathResource implements Resource {

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
