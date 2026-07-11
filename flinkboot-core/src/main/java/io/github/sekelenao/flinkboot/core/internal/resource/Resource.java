package io.github.sekelenao.flinkboot.core.internal.resource;

import io.github.sekelenao.flinkboot.core.api.exception.resource.UnrecognizedResourceException;

import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

public interface Resource {

    static Resource get(String location){
        Objects.requireNonNull(location);
        var index = location.indexOf(':');
        if(index == -1 || index == 0){
            throw new UnrecognizedResourceException(location);
        }
        var prefix = location.substring(0, index).toLowerCase(Locale.ROOT);
        var suffix = location.substring(index + 1);
        if(prefix.equals("classpath") || prefix.equals("resource")){
            return new ClasspathResource(suffix);
        }
        if(prefix.equals("file")){
            return new FileSystemResource(suffix);
        }
        throw new UnrecognizedResourceException(location);
    }

    InputStream inputStream();

}
