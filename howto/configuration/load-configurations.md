# How to Load & Merge Configurations

Flinkboot allows you to load, merge, and validate YAML configuration files into strongly-typed Java models at startup.

---

## Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add the core Flinkboot dependency along with Flink APIs:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>${flinkboot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Flinkboot Core -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-core</artifactId>
    </dependency>

    <!-- Flink Streaming API (Provided by Flink cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>
</dependencies>
```

---

## 1. Default Configuration Location & YAML Structure

### Default File Location
By default, when you initialize Flinkboot, it looks for a configuration file located at:

```text
classpath:job-configuration.yaml
```

*(i.e., a file named `job-configuration.yaml` in your application's JAR classpath / `src/main/resources/`).*

### Specifying Custom or Multiple Files
You can override the default location or supply multiple comma-separated configuration files using the `-flinkboot-configurations` command-line argument or the `FLINKBOOT_CONFIGURATIONS` environment variable:

```bash
# Via Command Line (CLI)
flink run MyJob.jar -flinkboot-configurations "file:base.yaml,file:override.yaml"

# Via Environment Variable
export FLINKBOOT_CONFIGURATIONS="file:base.yaml,file:override.yaml"
flink run MyJob.jar
```

*Supported location URI schemes:*
- `file:<path>` — Path on the local filesystem (e.g., `file:/etc/flink/job-configuration.yaml` or `file:/tmp/override.yaml`).
- `classpath:<path>` — Resource file inside the JAR classpath (e.g., `classpath:job-configuration.yaml`).
- `resource:<path>` — Alias for classpath resources.

> [!NOTE]
> Configuration resolution is powered by Flinkboot's unified `Resource` API. See the [How to Load Resources](load-resources.md) guide for comprehensive details on resource loading.

---

## 2. YAML Properties & Structure

Define your configuration properties in your YAML file (`job-configuration.yaml`):

```yaml
jobName: "my-analytics-pipeline"
parallelism: 8
bufferTimeoutMs: 100
```

---

## 3. Java Model & Loading

### Defining Your Configuration Model

Define your configuration model as an immutable Java class (or Record) annotated with Jackson and Jakarta Bean Validation annotations:

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class MyJobConfig {

    @NotBlank
    private final String jobName;

    @Min(1)
    private final int parallelism;

    private final long bufferTimeoutMs;

    @JsonCreator
    public MyJobConfig(
        @JsonProperty("jobName") String jobName,
        @JsonProperty("parallelism") int parallelism,
        @JsonProperty("bufferTimeoutMs") long bufferTimeoutMs
    ) {
        this.jobName = jobName;
        this.parallelism = parallelism;
        this.bufferTimeoutMs = bufferTimeoutMs;
    }

    public String jobName() { return jobName; }
    public int parallelism() { return parallelism; }
    public long bufferTimeoutMs() { return bufferTimeoutMs; }
}
```

### Loading in Java

Use `Flinkboot.initialize(args).configuration(...)` in your main class to load, merge, and validate your configuration:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;

public class MyFlinkJob {
    public static void main(String[] args) throws Exception {
        // Initialize Flinkboot with CLI args
        Flinkboot boot = Flinkboot.initialize(args);

        // Load configuration (defaults to classpath:job-configuration.yaml)
        MyJobConfig config = boot.configuration(MyJobConfig.class);

        System.out.println("Loaded Job: " + config.jobName());
    }
}
```

### Customizing the Jackson YAML Mapper

If you need custom Jackson deserialization features, pass a customizer `Consumer<YAMLMapper.Builder>` or a pre-configured `YAMLMapper`:

```java
// Option A: Using a builder customizer
MyJobConfig config = boot.configuration(MyJobConfig.class, builder -> {
    builder.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
});

// Option B: Using a pre-configured mapper
YAMLMapper customMapper = new YAMLMapper();
MyJobConfig config = boot.configuration(MyJobConfig.class, customMapper);
```

---

## 4. Environment Variable Placeholders

You can interpolate environment variables directly in your YAML configuration files using the `${VARIABLE_NAME}` syntax:

```yaml
database:
  url: "jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
  username: "${DB_USER}"
  password: "${DB_PASSWORD}"

kafka-source:
  bootstrap-servers:
    - "${KAFKA_BOOTSTRAP_SERVERS}"
  group-id: "${KAFKA_GROUP_ID}"
```

### Strict Fail-Fast Policy
To prevent silent production misconfigurations (e.g. connecting to a default cluster due to a forgotten environment variable), Flinkboot adheres to a **strict fail-fast principle**:
- If any referenced environment variable is missing, Flinkboot will **immediately abort startup** and throw an `UnresolvedPropertyPlaceholderException` indicating the missing variable name.
- Default fallback syntax (e.g. `${VAR:default}`) is intentionally unsupported.

### Case Normalization
Placeholder names are automatically normalized to standard Unix environment variable naming conventions (`SCREAMING_SNAKE_CASE`):
- `${kafka-bootstrap-servers}` $\rightarrow$ queries `KAFKA_BOOTSTRAP_SERVERS`
- `${kafka.bootstrap.servers}` $\rightarrow$ queries `KAFKA_BOOTSTRAP_SERVERS`
- `${KAFKA_BOOTSTRAP_SERVERS}` $\rightarrow$ queries `KAFKA_BOOTSTRAP_SERVERS`

### Escaping Literal Placeholders
If your application requires a literal `${...}` string (e.g. for S3 partition path templates, Logback/Log4j patterns, or regexes), escape it with a backslash `\${...}`:

```yaml
storage:
  s3-partition-path: "year=\${year}/month=\${month}/day=\${day}"
```

* **Result in Java:** `"year=${year}/month=${month}/day=${day}"` (the leading backslash is stripped and no environment lookup is performed).

---

## 5. Merging Semantics & CLI Flags

When multiple files are specified (e.g. `file:base.yaml,file:override.yaml`), Flinkboot merges them sequentially from left to right.

### Default Behavior: Strict Merging
By default, Flinkboot enforces strict merging rules to prevent accidental configuration overrides:
- **Scalar Overrides:** Overriding an existing key is forbidden by default. If a key is redefined in a later file, a `YamlParsingException` is thrown.
- **Nested Objects:** Nested objects are merged recursively (deep merge).
- **Lists and Arrays:** Redefining a list key in a later file is treated as a scalar override and throws a `YamlParsingException` by default.

### Merge Control Flags

You can customize the merging behavior using command-line flags or environment variables:

#### A. Permitting Property Overrides
Use `--flinkboot-configuration-override` (or `FLINKBOOT_CONFIGURATION_OVERRIDE=true`) to allow properties to be overwritten by subsequent files:

```bash
flink run MyJob.jar -flinkboot-configurations "file:base.yaml,file:env-override.yaml" --flinkboot-configuration-override
```

* **`base.yaml`:** `parallelism: 4`
* **`env-override.yaml`:** `parallelism: 16`
* **Result:** `parallelism: 16`

#### B. Permitting List Merging (Appends)
Use `--flinkboot-configuration-list-merging` (or `FLINKBOOT_CONFIGURATION_LIST_MERGING=true`) to append list items from subsequent files together:

```yaml
# base.yaml
topics:
  - "users"

# override.yaml
topics:
  - "orders"
```

* **Result with list merging enabled:** `topics: ["users", "orders"]`

> [!CAUTION]
> If you set boolean flags via environment variables (e.g. `FLINKBOOT_CONFIGURATION_OVERRIDE=true`), values must strictly be `"true"` or `"false"`. Any other string (e.g., `"yes"` or `"1"`) will fail fast at startup with a `BooleanParsingException`.

---

## 6. Validation & Parsing Behaviors

- **Fail-Fast & Multi-Line Validation:** After loading and merging files, Flinkboot validates the root object against Jakarta Bean Validation annotations. If validation fails, a `ConfigurationValidationException` is thrown displaying violations as a structured, alphabetically-sorted bullet list.
- **Configurable Violations Log Size:** By default, up to 10 validation errors are displayed before summary truncation (`- ... and X more violation(s)`) to prevent terminal and log pollution. This threshold can be adjusted using `-flinkboot-configuration-violations-log-size <number>` (or `FLINKBOOT_CONFIGURATION_VIOLATIONS_LOG_SIZE=<number>`).
- **Strict Property Parsing:** Any property in your YAML file that does not match a field in your Java class will cause a `YamlParsingException`. This catches typos immediately.
- **Case-Insensitive Keys & Enums:** Property names and Enum values are matched case-insensitively.
- **Native Java 8 Date/Time Support:** Java 8+ temporal types (`java.time.Duration`, `java.time.Instant`, `java.time.LocalDate`, etc.) are natively supported out-of-the-box in YAML models without extra configuration.
- **Jackson Module Auto-Discovery:** Additional Jackson modules on the classpath are automatically discovered and registered via `findAndAddModules()`.

