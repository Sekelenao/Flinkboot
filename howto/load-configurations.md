# How to Load & Merge Configurations

Flinkboot allows you to load and recursively merge multiple configuration files into a single, validated Java configuration object at startup.

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

### Value Overrides
If a property is present in both files, the value from the later file overrides the earlier one:
* **`base.yaml`:** `parallelism: 4`
* **`override.yaml`:** `parallelism: 16`
* **Result:** `parallelism: 16`

### Nested Objects
Nested objects are merged recursively (deep merge):
* **`base.yaml`:** 
  ```yaml
  database:
    host: "localhost"
    port: 5432
  ```
* **`override.yaml`:** 
  ```yaml
  database:
    host: "db-prod"
  ```
* **Result:**
  ```yaml
  database:
    host: "db-prod"
    port: 5432
  ```

### Lists and Arrays
By default, list elements are **appended** during a merge:
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

---

## 3. Strict Validation

After all configurations have been merged, Flinkboot validates the final object against Jakarta Bean Validation constraints (e.g., `@NotBlank`, `@Min`, `@NotNull`).

- If validation fails, Flinkboot throws a `ConfigurationValidationException` listing up to 3 violation details.
- If a file is completely empty or contains only comments, it is safely ignored during merging.
- If the configuration source is non-empty but does not resolve to a root YAML object (e.g., a YAML list or primitive value), a `YamlParsingException` is thrown.
