# How to Load & Merge Configurations

Flinkboot allows you to load and recursively merge multiple configuration files into a single, validated Java configuration object at startup.

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

Use the `configuration(Class<C> configurationClass)` method of the [Flinkboot](file:///home/haine/Documents/Programmation/Flinkboot/flinkboot-core/src/main/java/io/github/sekelenao/flinkboot/core/api/Flinkboot.java) instance to load your configuration:

### Defining the Configuration Model

Define your configuration as an immutable Java class with fluent accessor methods and a Jackson creator constructor:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class JobConfig {

    @NotBlank 
    private final String jobName;

    @Min(1) 
    private final int parallelism;

    @JsonCreator
    public JobConfig(
        @JsonProperty("jobName") String jobName,
        @JsonProperty("parallelism") int parallelism
    ) {
        this.jobName = jobName;
        this.parallelism = parallelism;
    }

    public String jobName() { return jobName; }
    public int parallelism() { return parallelism; }
}
```

### Loading the Configuration

Use the `configuration(Class<C> configurationClass)` method to load, merge, and validate your configuration:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;

public class MyFlinkJob {
    public static void main(String[] args) throws Exception {
        // Load and validate configurations
        JobConfig config = Flinkboot.initialize(args).configuration(JobConfig.class);
        
        System.out.println("Loaded job: " + config.jobName());
    }
}
```

### Customizing the YAML Mapper

If you need to customize Jackson's deserialization settings, you can pass a customizer `Consumer<YAMLMapper.Builder>` or a pre-configured `YAMLMapper` directly:

```java
// Option A: Using a builder customizer
JobConfig config = Flinkboot.initialize(args)
    .configuration(JobConfig.class, builder -> {
        builder.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    });

// Option B: Using a pre-configured mapper
YAMLMapper customMapper = new YAMLMapper();
JobConfig config = Flinkboot.initialize(args)
    .configuration(JobConfig.class, customMapper);
```

---

## 2. Merging Semantics

When multiple files are specified in `-flinkboot-configurations` (e.g. `file:base.yaml,file:override.yaml`), Flinkboot merges them sequentially from left to right.

### Default Behavior: Strict Merging
By default, Flinkboot enforces strict merging rules to prevent accidental configuration overrides:
* **Value Overrides:** Overriding an existing scalar key or list is forbidden. If a key is redefined in a later file, a `YamlParsingException` is thrown.
* **Nested Objects:** Nested objects are merged recursively (deep merge) as long as there are no scalar/list conflicts.
* **Lists and Arrays:** Re-defining a list key in a later file is treated as a value override and will throw a `YamlParsingException` by default.

### Customizing Merging Behavior (Flags)
You can customize the merging behavior using the following command-line flags or environment variables:

#### A. Permitting Overrides
Use the flag `--flinkboot-configuration-override` (or environment variable `FLINKBOOT_CONFIGURATION_OVERRIDE=true`) to allow properties to be overwritten.
* **With override enabled:**
  * **`base.yaml`:** `parallelism: 4`
  * **`override.yaml`:** `parallelism: 16`
  * **Result:** `parallelism: 16` (instead of throwing an exception)
  * **Lists and Arrays:** The entire list from the later file completely replaces the list from the earlier file.

#### B. Permitting List Merging
Use the flag `--flinkboot-configuration-list-merging` (or environment variable `FLINKBOOT_CONFIGURATION_LIST_MERGING=true`) to allow list elements to be appended together during a merge instead of being replaced.
* **With list merging enabled:**
  * **`base.yaml`:**
    ```yaml
    topics:
      - "users"
    ```
  * **`override.yaml`:**
    ```yaml
    topics:
      - "orders"
    ```
  * **Result:** `topics: ["users", "orders"]`
  * *Note:* If list merging is enabled but `--flinkboot-configuration-override` is disabled, scalar overrides will still throw an exception, but list merges (appends) are allowed. If both are enabled, both scalar overrides and list appends are allowed.
  * *Strict Boolean Rule:* Just like any other Flinkboot flags, if you set `FLINKBOOT_CONFIGURATION_OVERRIDE` or `FLINKBOOT_CONFIGURATION_LIST_MERGING` via environment variables, their values must be strictly `"true"` or `"false"`. Any other value will cause Flinkboot to fail fast with a `BooleanParsingException`.

---

## 3. Strict Validation

After all configurations have been merged, Flinkboot validates the final object against Jakarta Bean Validation constraints (e.g., `@NotBlank`, `@Min`, `@NotNull`).

- If validation fails, Flinkboot throws a `ConfigurationValidationException` listing up to 3 violation details.
- If a file is completely empty or contains only comments, it is safely ignored during merging.
- If the configuration source is non-empty but does not resolve to a root YAML object (e.g., a YAML list or primitive value), a `YamlParsingException` is thrown.

---

## 4. Default Parser Settings

By default, Flinkboot's parser configures the underlying `YAMLMapper` with the following behaviors:

- **Strict Property Parsing** — Any property defined in your YAML that is not declared in your Java class will cause a `YamlParsingException`. This helps catch spelling mistakes immediately at startup.
- **Case-Insensitive Properties** — Property keys in YAML are resolved case-insensitively (e.g., `parallelism` and `PARALLELISM` both map to the class field `parallelism`).
- **Case-Insensitive Enums** — Deserialized enum values are matched case-insensitively (e.g., the string `"streaming"` maps to `JobType.STREAMING`).
- **Automatic Module Discovery** — Flinkboot calls `.findAndAddModules()` on the mapper builder. Any Jackson modules available on the classpath (e.g., for Java 8 date/time libraries, parameter names, or custom types) are automatically registered.
