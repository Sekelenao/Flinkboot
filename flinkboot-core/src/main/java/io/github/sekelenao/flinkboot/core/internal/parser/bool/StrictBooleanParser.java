package io.github.sekelenao.flinkboot.core.internal.parser.bool;

import io.github.sekelenao.flinkboot.core.api.exception.parsing.BooleanParsingException;

import java.util.Locale;

public final class StrictBooleanParser {

    private StrictBooleanParser() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean parse(String value) {
        if(value.toLowerCase(Locale.ROOT).equals("true")) {
            return true;
        }
        if(value.toLowerCase(Locale.ROOT).equals("false")) {
            return false;
        }
        throw new BooleanParsingException("Invalid boolean value: " + value);
    }

}
