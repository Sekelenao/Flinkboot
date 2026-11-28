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
        if (cleanPath.isEmpty()) {
            throw new ResourceNotFoundException(location);
        }
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            var stream = contextClassLoader.getResourceAsStream(cleanPath);
            if (stream != null) {
                return stream;
            }
        }
        var fallbackClassLoader = ClasspathResource.class.getClassLoader();
        if (fallbackClassLoader != null && fallbackClassLoader != contextClassLoader) {
            var stream = fallbackClassLoader.getResourceAsStream(cleanPath);
            if (stream != null) {
                return stream;
            }
        }
        throw new ResourceNotFoundException(location);
    }

}
