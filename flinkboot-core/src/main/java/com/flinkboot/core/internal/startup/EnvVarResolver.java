package com.flinkboot.core.internal.startup;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class EnvVarResolver {

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[-.]");

    private final UnaryOperator<String> getter;

    public EnvVarResolver(UnaryOperator<String> getter){
        this.getter = Objects.requireNonNull(getter);
    }

    public Optional<String> get(String key){
        Objects.requireNonNull(key);
        var varName = SEPARATOR_PATTERN.matcher(key).replaceAll("_").toUpperCase(Locale.ROOT);
        return Optional.ofNullable(getter.apply(varName));
    }

}
