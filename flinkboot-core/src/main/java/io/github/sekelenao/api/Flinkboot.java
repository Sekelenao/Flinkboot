package io.github.sekelenao.api;

import io.github.sekelenao.internal.startup.StartupEnvironment;

import java.util.Objects;

public final class Flinkboot {

    private final StartupEnvironment startupEnvironment;

    private Flinkboot(String[] args) {
        this.startupEnvironment = new StartupEnvironment(args);
    }

    public static Flinkboot initialize(String[] args){
        Objects.requireNonNull(args);
        return new Flinkboot(args);
    }

}
