# How to Load Configurations in Tests

Flinkboot provides test utilities in `flinkboot-test` to easily load, merge, and validate YAML configurations directly within your JUnit 5 tests.

---

## 1. Overview

When writing unit or integration tests for your Flink applications, you often need to load and validate your application configuration objects (DTOs) without starting a full command-line application.

The `FlinkbootTest.configuration(...)` helper method allows you to load and validate configuration classes directly in a single, clean line of Java code.

Key features:
* **Direct DTO Instantiation**: Instantiates, merges, and validates your custom configuration object.
* **Explicit Scheme Prefixes**: Supports `classpath:`, `resource:`, and `file:` schemes.
* **Multi-File Merging**: Accepts varargs of configuration locations to test multi-file environments.
* **Unchecked Exceptions**: Wraps `IOException` in `UncheckedIOException` so test methods do not require boilerplate `throws` clauses.

---

## 2. Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add `flinkboot-test` in your `pom.xml`:

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
    <!-- Flinkboot Test Utilities -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-test</artifactId>
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
import io.github.sekelenao.flinkboot.test.api.FlinkbootTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationConfigTest {

    @Test
    @DisplayName("Should load application configuration from classpath YAML")
    void testLoadConfiguration() {
        MyApplicationConfig config = FlinkbootTest.configuration(
            MyApplicationConfig.class, 
            "classpath:job-test.yaml"
        );

        assertNotNull(config);
        assertEquals("unit-test-job", config.job().name());
    }
}
```

---

### Loading Multiple Configuration Files

You can pass multiple configuration paths as varargs to test profile overrides or multi-file setups:

```java
@Test
@DisplayName("Should load and merge base and environment override configurations")
void testLoadMultipleConfigurations() {
    MyApplicationConfig config = FlinkbootTest.configuration(
        MyApplicationConfig.class,
        "classpath:config-base.yaml",
        "classpath:config-test-env.yaml"
    );

    assertNotNull(config);
}
```

---

### Loading Files from the File System

You can also target files outside the classpath using the `file:` scheme prefix:

```java
@Test
@DisplayName("Should load configuration from local file system")
void testLoadFromFileSystem() {
    MyApplicationConfig config = FlinkbootTest.configuration(
        MyApplicationConfig.class,
        "file:/etc/flinkboot/my-config.yaml"
    );

    assertNotNull(config);
}
```

---

## 4. Scheme Prefix Requirement

Each path passed to `FlinkbootTest.configuration(...)` **must explicitly specify a resource scheme prefix**:

| Scheme Prefix | Target Location | Example |
|:---|:---|:---|
| `classpath:` | Classpath resources (e.g. `src/test/resources`) | `"classpath:job-test.yaml"` |
| `resource:` | Alias for classpath resources | `"resource:job-test.yaml"` |
| `file:` | Absolute or relative file system paths | `"file:/tmp/test-config.yaml"` |

> [!IMPORTANT]
> Omitting the scheme prefix (e.g., passing `"job-test.yaml"` without `classpath:`) will throw an `UnrecognizedResourceException`. Always include `classpath:` or `file:`.
