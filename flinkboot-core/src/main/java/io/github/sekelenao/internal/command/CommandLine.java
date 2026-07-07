package io.github.sekelenao.internal.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class CommandLine {

    private final Map<String, String> options;

    private final Set<String> flags;

    private CommandLine(Map<String, String> options, Set<String> flags) {
        this.options = options;
        this.flags = flags;
    }

    private static String retrieveValue(String[] args, int keyIndex){
        if(keyIndex + 1 >= args.length){
            throw new NoSuchElementException("No value found for option: " + args[keyIndex]);
        }
        return args[keyIndex + 1];
    }

    public static CommandLine parse(String[] args) {
        Objects.requireNonNull(args);
        var options = new HashMap<String, String>();
        var flags = new HashSet<String>();
        for (int i = 0; i < args.length; i++) {
            var argument = args[i];
            if (argument.startsWith("--")) {
                flags.add(argument.substring(2).toLowerCase(Locale.ROOT));
            } else if (argument.startsWith("-")) {
                options.put(argument.substring(1).toLowerCase(Locale.ROOT), retrieveValue(args, i++));
            }
        }
        return new CommandLine(options, flags);
    }

    public Optional<String> option(String option){
        return Optional.ofNullable(options.get(option.toLowerCase(Locale.ROOT)));
    }

    public boolean flag(String flag){
        return flags.contains(flag.toLowerCase(Locale.ROOT));
    }

}
