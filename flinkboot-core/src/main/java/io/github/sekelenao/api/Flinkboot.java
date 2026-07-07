package io.github.sekelenao.api;

import io.github.sekelenao.internal.command.CommandLine;

import java.util.Objects;

public final class Flinkboot {

    private static final String CONFIG_LOCATION_PROPERTY = "flinkboot-config";

    private final CommandLine commandLine;

    private Flinkboot(String[] args) {
        this.commandLine = CommandLine.parse(args);
    }

    public static Flinkboot initialize(String[] args){
        Objects.requireNonNull(args);
        return new Flinkboot(args);
    }

}
