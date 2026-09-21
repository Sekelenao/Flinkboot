package io.github.sekelenao.flinkboot.core.internal.resource;

import io.github.sekelenao.flinkboot.core.internal.annotation.VisibleForTesting;

import java.nio.file.FileSystems;
import java.util.Objects;
import java.util.regex.Pattern;

public final class FileSystemSupports {

    private static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^[\\\\/]+(?=[a-zA-Z]:)");

    private FileSystemSupports() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public enum FileSystemType {POSIX, WINDOWS}

    public static FileSystemType type() {
        if ("\\".equals(FileSystems.getDefault().getSeparator())) {
            return FileSystemType.WINDOWS;
        }
        return FileSystemType.POSIX;
    }

    public static String normalize(String location) {
        return normalize(location, type());
    }

    @VisibleForTesting
    static String normalize(String location, FileSystemType type) {
        Objects.requireNonNull(location, "location must not be null");
        Objects.requireNonNull(type, "type must not be null");
        if (type == FileSystemType.WINDOWS) {
            return WINDOWS_DRIVE_PATTERN.matcher(location).replaceFirst("");
        }
        return location;
    }

}
