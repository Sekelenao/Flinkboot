package io.github.sekelenao.flinkboot.core.internal.parser.integer;

import java.util.Objects;
import java.util.function.Supplier;

public final class IntegerParser {

    private IntegerParser() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static int parse(String value, Supplier<? extends RuntimeException> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw supplier.get();
        }
    }

    public static int parseStrictlyPositive(String value, Supplier<? extends RuntimeException> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        var parsed = parse(value, supplier);
        if (parsed <= 0) {
            throw supplier.get();
        }
        return parsed;
    }
}
