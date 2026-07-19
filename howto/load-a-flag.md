# How to Load a Flag with Flinkboot

In Flinkboot, a **flag** is a simple boolean switch. It allows you to enable or disable features or modes (such as a debug mode, a dry-run mode, etc.). By default, if the flag is not provided, its value is `false`.

> [!IMPORTANT]
> Before defining your custom flags, please check the [Reserved Keys](reserved-keys.md) page to avoid naming collisions with Flinkboot's built-in parameters.

## Maven Dependency

To use this feature, import the core Flinkboot dependency:

```xml
<dependency>
    <groupId>io.github.sekelenao</groupId>
    <artifactId>flinkboot-core</artifactId>
</dependency>
```

---

## 1. Usage in Java Code

To check the presence or value of a flag in your Flink job, use the `flag(String name)` method of the [Flinkboot](file:///home/haine/Documents/Programmation/Flinkboot/flinkboot-core/src/main/java/io/github/sekelenao/flinkboot/core/api/Flinkboot.java) instance:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;

public class MyFlinkJob {
    public static void main(String[] args) {
        // Initialize Flinkboot
        Flinkboot flinkboot = Flinkboot.initialize(args);

        // Retrieve a flag named "debug"
        boolean isDebug = flinkboot.flag("debug");

        if (isDebug) {
            System.out.println("Debug mode activated!");
        }
    }
}
```

---

## 2. Define the Flag via Command Line (CLI)

To enable a flag when starting your job, pass it as a command-line argument prefixed with double dashes `--`:

```bash
flink run MyJobJar.jar --debug
```

*Note: The presence of the `--debug` argument is sufficient to set the flag to `true`.*

---

## 3. Define the Flag via Environment Variables

You can also enable a flag using an environment variable. Flinkboot automatically resolves key names using the following convention:
1. Converts the key to uppercase.
2. Replaces dashes (`-`) and dots (`.`) with underscores (`_`).

### Key Mapping Example:
* Flag name in Java code: `dry-run`
* Corresponding environment variable name: `DRY_RUN`

To define the `dry-run` flag via the environment, set the variable to `true` or `false` (case-insensitive):

```bash
# Enable the flag
export DRY_RUN=true

# Disable the flag explicitly
export DRY_RUN=false

flink run MyJobJar.jar
```

> [!CAUTION]
> Flinkboot enforces strict boolean parsing. If you set the environment variable to an invalid boolean value (e.g., `DRY_RUN=yes`, `DRY_RUN=1`, or any value other than `true` or `false`), Flinkboot will fail fast at startup and throw a `BooleanParsingException`.

---

## 4. Precedence & Default Value

When resolving a flag's value, Flinkboot applies the following order of precedence (from highest to lowest):

1. **Command Line Option:** If `--debug` is present in the CLI arguments, the flag is `true`.
2. **Environment Variable:** If the CLI option is absent, Flinkboot looks up the corresponding environment variable (`DEBUG`).
   * If the value is `"true"` (case-insensitive), the flag is `true`.
   * If the value is `"false"` (case-insensitive), the flag is `false`.
   * If the value is anything else, Flinkboot throws a `BooleanParsingException`.
3. **Default Value:** If the flag is defined neither in the CLI nor in the environment, it defaults to `false`.
