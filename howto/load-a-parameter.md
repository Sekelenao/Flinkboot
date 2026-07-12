# How to Load a Parameter with Flinkboot

In Flinkboot, a **parameter** is a key-value pair where the value is a string. Parameters are retrieved as an `Optional<String>`.

> [!IMPORTANT]
> Before defining your custom parameters, please check the [Reserved Keys](reserved-keys.md) page to avoid naming collisions with Flinkboot's built-in configuration keys.

---

## 1. Usage in Java Code

To retrieve a parameter's value in your Flink job, use the `parameter(String name)` method of the [Flinkboot](file:///home/haine/Documents/Programmation/Flinkboot/flinkboot-core/src/main/java/io/github/sekelenao/flinkboot/core/api/Flinkboot.java) instance:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import java.util.Optional;

public class MyFlinkJob {
    public static void main(String[] args) {
        // Initialize Flinkboot
        Flinkboot flinkboot = Flinkboot.initialize(args);

        // Retrieve a parameter named "db-url"
        Optional<String> dbUrl = flinkboot.parameter("db-url");

        dbUrl.ifPresent(url -> {
            System.out.println("Connecting to database: " + url);
        });
    }
}
```

---

## 2. Define the Parameter via Command Line (CLI)

To pass a parameter value when starting your job, prefix the parameter key with a single dash `-` followed by its value:

```bash
flink run MyJobJar.jar -db-url "jdbc:postgresql://localhost:5432/mydb"
```

*Note: In CLI arguments, keys are matched case-insensitively.*

---

## 3. Define the Parameter via Environment Variables

You can also specify a parameter using an environment variable. Flinkboot automatically resolves key names using the following convention:
1. Converts the key to uppercase.
2. Replaces dashes (`-`) and dots (`.`) with underscores (`_`).

### Key Mapping Example:
* Parameter name in Java code: `db-url`
* Corresponding environment variable name: `DB_URL`

To pass the parameter via the environment, set the variable to your desired string:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/mydb"
flink run MyJobJar.jar
```

---

## 4. Precedence & Default Behavior

When resolving a parameter's value, Flinkboot applies the following order of precedence (from highest to lowest):

1. **Command Line Option:** If `-db-url <value>` is present in the CLI arguments, the parameter takes that value.
2. **Environment Variable:** If the CLI option is absent, Flinkboot looks up the corresponding environment variable (`DB_URL`).
3. **Empty Optional:** If the parameter is defined neither in the CLI nor in the environment, it returns `Optional.empty()`. You can handle defaults in Java using `Optional.orElse("default")` or `Optional.orElseThrow()`.
