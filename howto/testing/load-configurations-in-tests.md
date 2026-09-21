# How to Load Configurations in Tests

Flinkboot allows you to easily load, merge, and validate YAML configurations directly within your JUnit 5 tests using `Flinkboot.initialize(...)`.

---

## 1. Overview

When writing unit or integration tests for your Flink applications, you often need to load and validate your application configuration objects (DTOs) without starting a full command-line application.

Using `Flinkboot.initialize(...)` directly in tests provides:
* **Production Parity**: Tests execute through the exact same startup and parsing engine used in production.
* **Varargs Simplicity**: Call `Flinkboot.initialize()` with zero arguments for default classpath configuration, or pass command-line options inline.
* **Full Option Support**: Test custom flags (`--dry-run`), CLI parameters (`-threshold 100`), or Flinkboot runtime options (`--flinkboot-configuration-disable-validation`) alongside your configuration files.
* **Explicit Scheme Prefixes**: Unified support for `classpath:`, `resource:`, and `file:` schemes via Flinkboot's `Resource` API.

---

## 2. Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add `flinkboot-core` in your `pom.xml`:

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

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 3. Usage Examples

### Loading a Single Classpath Configuration

Place your test YAML configuration in `src/test/resources/job-test.yaml`:

```yaml
job:
  name: "unit-test-job"
  environment:
    execution:
      parallelism: 2
```

In your JUnit 5 test class:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationConfigTest {

    @Test
    @DisplayName("Should load application configuration from classpath YAML")
    void testLoadConfiguration() throws Exception {
        MyApplicationConfig config = Flinkboot.initialize(
            "-flinkboot-configurations", "classpath:job-test.yaml"
        ).configuration(MyApplicationConfig.class);

        assertNotNull(config);
        assertEquals("unit-test-job", config.job().name());
    }
}
```

---

### Loading the Default Classpath Configuration

If your test relies on the default configuration file (`src/test/resources/job-configuration.yaml`), you can call `Flinkboot.initialize()` with no arguments:

```java
@Test
@DisplayName("Should load default configuration from classpath:job-configuration.yaml")
void testLoadDefaultConfiguration() throws Exception {
    MyApplicationConfig config = Flinkboot.initialize()
        .configuration(MyApplicationConfig.class);

    assertNotNull(config);
}
```

---

### Loading Multiple Configuration Files

You can pass multiple comma-separated configuration paths to test profile overrides or multi-file setups:

```java
@Test
@DisplayName("Should load and merge base and environment override configurations")
void testLoadMultipleConfigurations() throws Exception {
    MyApplicationConfig config = Flinkboot.initialize(
        "-flinkboot-configurations",
        "classpath:config-base.yaml,classpath:config-test-env.yaml"
    ).configuration(MyApplicationConfig.class);

    assertNotNull(config);
}
```

---

### Loading Files from the File System

You can also target files outside the classpath using the `file:` scheme prefix:

```java
@Test
@DisplayName("Should load configuration from local file system")
void testLoadFromFileSystem() throws Exception {
    MyApplicationConfig config = Flinkboot.initialize(
        "-flinkboot-configurations", "file:/etc/flinkboot/my-config.yaml"
    ).configuration(MyApplicationConfig.class);

    assertNotNull(config);
}
```

---

### Testing with Flags and CLI Parameters

Because `Flinkboot.initialize(String... args)` takes standard command-line arguments, you can easily test configuration behavior alongside custom flags and parameters:

```java
@Test
@DisplayName("Should load configuration with flags and parameters")
void testLoadConfigurationWithOptions() throws Exception {
    Flinkboot boot = Flinkboot.initialize(
        "-flinkboot-configurations", "classpath:job-test.yaml",
        "--dry-run",
        "-custom-param", "custom-value"
    );

    MyApplicationConfig config = boot.configuration(MyApplicationConfig.class);

    assertAll(
        () -> assertNotNull(config),
        () -> assertTrue(boot.flag("dry-run")),
        () -> assertEquals("custom-value", boot.parameter("custom-param").orElseThrow())
    );
}
```

---

## 4. Scheme Prefix Requirement

Each path passed to `-flinkboot-configurations` **must explicitly specify a resource scheme prefix**:

| Scheme Prefix | Target Location                                 | Example                        |
|:--------------|:------------------------------------------------|:-------------------------------|
| `classpath:`  | Classpath resources (e.g. `src/test/resources`) | `"classpath:job-test.yaml"`    |
| `resource:`   | Alias for classpath resources                   | `"resource:job-test.yaml"`     |
| `file:`       | Absolute or relative file system paths          | `"file:/tmp/test-config.yaml"` |

> [!IMPORTANT]
> Omitting the scheme prefix (e.g., passing `"job-test.yaml"` without `classpath:`) will throw an `UnrecognizedResourceException`. Always include `classpath:` or `file:`. See the [How to Load Resources](../configuration/load-resources.md) guide for more information on the underlying `Resource` abstraction.

---

## Related Guides

* [How to Assert Java Serialization Compliance](assert-serialization-compliance.md)
* [How to Assert Flink POJO Compliance](assert-pojo-compliance.md)
* [How to Collect Stream Elements in Tests](collect-stream-elements-in-tests.md)
