package io.github.sekelenao.internal.startup;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class EnvVarResolver {

    private EnvVarResolver(){
        throw new AssertionError("You cannot instantiate this class");
    }

    public static Optional<String> get(String key){
        Objects.requireNonNull(key);
        var varName = key.replaceAll("[-.]", "_").toUpperCase(Locale.ROOT);
        return Optional.ofNullable(System.getenv(varName));
    }

}
