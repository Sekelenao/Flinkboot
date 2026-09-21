# How to Assert Java Serialization Compliance

Flinkboot provides testing assertions in `flinkboot-test` to verify that your configuration classes, operators, and helper objects comply with standard Java serialization requirements.

---

## 1. Why Java Serialization Matters in Apache Flink

In Apache Flink streaming architectures, user-defined functions (`ProcessFunction`, `MapFunction`, `Sink`, `RichFlatMapFunction`, etc.) are instantiated on the client or JobManager and then distributed across remote TaskManagers over the network.

Flink uses standard Java serialization (`java.io.ObjectOutputStream` / `java.io.ObjectInputStream`) to ship these operators and any objects captured in their fields:
* **Application Configurations**: DTOs or properties classes stored in operator fields to configure thresholds, endpoints, or feature flags.
* **Helper Objects**: Evaluators, state descriptors, or custom logic components referenced by user-defined functions.

If any captured object (or any nested field/type inside its object graph) does not implement `java.io.Serializable` or contains non-serializable fields, Flink fails immediately upon job submission or initialization with a fatal `java.io.NotSerializableException`.

Catching serialization failures at development time with unit tests prevents costly runtime failures during cluster deployment.

---

## 2. Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add `flinkboot-test` to your `pom.xml`:

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

    <!-- Flink Streaming API (Provided by Flink cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>
</dependencies>
```

---

## 3. How `isSerializable()` Works

The assertion `FlinkbootAssertions.assertThat(actual).isSerializable()` performs a complete Java serialization round-trip:

1. **Serialization**: Writes the target object into an in-memory byte buffer using `ObjectOutputStream`.
2. **Deserialization**: Reads and reconstructs the object from the byte buffer using `ObjectInputStream`.

This verifies that:
* The target class implements `java.io.Serializable`.
* Every nested object, collection element, and referenced type in the object graph is serializable.
* Custom `writeObject` or `readObject` hooks (if present) execute without errors.

If any element fails serialization, the assertion fails immediately with an descriptive error detailing the root cause.

---

## 4. Usage Examples

### Testing Configuration Objects Loaded from YAML

Application configuration classes are often passed directly into operator constructors:

```java
import io.github.sekelenao.flinkboot.test.api.FlinkbootTest;
import io.github.sekelenao.flinkboot.test.api.assertion.FlinkbootAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JobConfigurationTest {

    @Test
    @DisplayName("Should verify that application configuration is serializable for Flink operators")
    void shouldBeSerializable() {
        MyApplicationConfig config = FlinkbootTest.configuration(
            MyApplicationConfig.class,
            "classpath:job-full-config.yaml"
        );

        FlinkbootAssertions.assertThat(config)
            .isSerializable();
    }
}
```

### Testing Custom Functions and Helper Objects

You can assert serialization compliance on any Java object or user-defined function:

```java
import io.github.sekelenao.flinkboot.test.api.assertion.FlinkbootAssertions;
import org.apache.flink.api.common.functions.MapFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

class ThresholdAlertFunctionTest {

    public static class ThresholdAlertFunction implements MapFunction<Long, String>, Serializable {
        private static final long serialVersionUID = 1L;

        private final long threshold;

        public ThresholdAlertFunction(long threshold) {
            this.threshold = threshold;
        }

        @Override
        public String map(Long value) {
            return value > threshold ? "ALERT: " + value : "OK";
        }
    }

    @Test
    @DisplayName("Should verify that ThresholdAlertFunction is serializable")
    void shouldBeSerializable() {
        ThresholdAlertFunction function = new ThresholdAlertFunction(100L);

        FlinkbootAssertions.assertThat(function)
            .isSerializable();
    }
}
```

---

## 5. Best Practices

* **Use a Complete YAML Fixture (`job-full-config.yaml`)**:
  When testing configuration classes loaded via `FlinkbootTest.configuration(...)`, ensure 100% of optional properties, nested DTOs, and collection elements are populated in the test YAML. Minimal fixtures that leave optional fields as `null` can mask non-serializable types until they are populated in production.
* **Mark Non-Serializable Resources as `transient`**:
  Fields holding network connections, thread pools, or client handles should be marked `transient` and initialized inside the operator's `open(OpenContext context)` or `open(Configuration parameters)` lifecycle method instead of being serialized.
* **Combine with POJO Assertions for Pipeline Elements**:
  Java serialization compliance (`isSerializable()`) is intended for **operators, functions, and configurations** shipped across the cluster. For **data records** traveling through Flink data streams, prefer [How to Assert Flink POJO Compliance](assert-pojo-compliance.md) to ensure high-performance native serialization without Kryo fallback.

---

## Related Guides

* [How to Assert Flink POJO Compliance](assert-pojo-compliance.md)
* [How to Load Configurations in Tests](load-configurations-in-tests.md)
* [How to Collect Stream Elements in Tests](collect-stream-elements-in-tests.md)
