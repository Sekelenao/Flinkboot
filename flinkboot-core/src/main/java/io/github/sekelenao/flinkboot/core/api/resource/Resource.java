package io.github.sekelenao.flinkboot.core.api.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.UnrecognizedResourceException;
import io.github.sekelenao.flinkboot.core.internal.resource.ClasspathResource;
import io.github.sekelenao.flinkboot.core.internal.resource.FileSystemResource;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

public interface Resource {

    static Resource of(String location) {
        Objects.requireNonNull(location);
        var trimmedLocation = location.strip();
        var index = trimmedLocation.indexOf(':');
        if (index <= 0) {
            throw new UnrecognizedResourceException(location);
        }
        var prefix = trimmedLocation.substring(0, index).toLowerCase(Locale.ROOT);
        var suffix = trimmedLocation.substring(index + 1).strip();
        if (suffix.isEmpty()) {
            throw new UnrecognizedResourceException(location);
        }
        if (prefix.equals("classpath") || prefix.equals("resource")) {
            return new ClasspathResource(suffix);
        }
        if (prefix.equals("file")) {
            return new FileSystemResource(suffix);
        }
        throw new UnrecognizedResourceException(location);
    }

    InputStream inputStream();

}
