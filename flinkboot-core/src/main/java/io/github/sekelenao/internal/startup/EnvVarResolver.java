package io.github.sekelenao.internal.startup;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class EnvVarResolver {

    private final UnaryOperator<String> getter;

    public EnvVarResolver(UnaryOperator<String> getter){
        this.getter = Objects.requireNonNull(getter);
    }

    public Optional<String> get(String key){
        Objects.requireNonNull(key);
        var varName = key.replaceAll("[-.]", "_").toUpperCase(Locale.ROOT);
        return Optional.ofNullable(getter.apply(varName));
    }

}
