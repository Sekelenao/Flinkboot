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
    public InputStream inputStream() throws IOException {
        Objects.requireNonNull(location);
        var stream = ClasspathResource.class.getResourceAsStream(location);
        if(stream == null){
            throw new ResourceNotFoundException(location);
        }
        return stream;
    }

}
