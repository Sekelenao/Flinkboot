# How to Configure a Kafka Source

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `KafkaSource` directly from YAML configuration files.

---

## Maven Dependencies

Import the Flinkboot BOM in your `<dependencyManagement>` and add the Flinkboot Kafka module along with Flink APIs:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sekelenao</groupId>
            <artifactId>flinkboot</artifactId>
            <version>0.1.0-1.20</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Flinkboot Kafka -->
    <dependency>
        <groupId>io.github.sekelenao</groupId>
        <artifactId>flinkboot-kafka</artifactId>
    </dependency>

    <!-- Flink Streaming API (Provided by Flink cluster) -->
    <dependency>
        <groupId>org.apache.flink</groupId>
        <artifactId>flink-streaming-java</artifactId>
    </dependency>
</dependencies>
```

---

## 1. YAML Configuration Properties & Structure

You can configure your Kafka Source using either a static list of topics or a topic pattern regex:

### Option A: Static List of Topics (`KafkaSourceTopicListProperties`)

```yaml
name: "my-kafka-source"
bootstrap-servers:
  - "localhost:9092"
group-id: "my-consumer-group"
topics:
  - "users"
  - "orders"
starting-offsets: "EARLIEST"
properties:
  session.timeout.ms: "45000"
```

### Option B: Topic Pattern Regex (`KafkaSourceTopicPatternProperties`)

```yaml
name: "my-kafka-source"
bootstrap-servers:
  - "localhost:9092"
group-id: "my-consumer-group"
topic-pattern: "^my-topic-.*$"
starting-offsets: "LATEST"
```

---

## 2. Configuration Parameters Reference

| Property Key                         | Type            | Required                    | Validation                        | Description                                                                                                                                         |
|:-------------------------------------|:----------------|:----------------------------|:----------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------|
| `name`                               | String          | **Yes**                     | `@NotBlank`                       | Logical name of the Kafka source configuration.                                                                                                     |
| `bootstrap-servers`                  | List of Strings | **Yes**                     | `@NotEmpty`, items `@NotBlank`     | Kafka bootstrap broker hosts/ports (e.g. `localhost:9092`).                                                                                         |
| `group-id`                           | String          | **Yes**                     | `@NotBlank`                       | Consumer group ID.                                                                                                                                  |
| `topics`                             | List of Strings | **Yes** (Only for Option A) | `@NotEmpty`, items `@NotBlank`     | Static list of topics to subscribe to.                                                                                                              |
| `topic-pattern`                      | String          | **Yes** (Only for Option B) | `@NotBlank`                       | Regex pattern to match topic subscriptions.                                                                                                         |
| `starting-offsets`                   | Enum            | **Yes**                     | `@NotNull` Enum                   | Strategy to start consuming. Supported values: `EARLIEST`, `LATEST`, `COMMITTED`, `COMMITTED_EARLIEST`, `COMMITTED_LATEST`, `TIMESTAMP`, `OFFSETS`. |
| `starting-offsets-timestamp`         | Long            | No                          | `@PositiveOrZero`                 | Timestamp in epoch milliseconds. **Mandatory** only if `starting-offsets` is set to `TIMESTAMP` (otherwise ignored). Must be positive or zero.      |
| `starting-offsets-partition-offsets` | List            | No                          | `@Valid` list items               | Specific partition offset offsets mapping. **Mandatory** only if `starting-offsets` is set to `OFFSETS` (otherwise ignored).                        |
| `properties`                         | Map             | No                          | Keys/values `@NotBlank`           | Custom Kafka client consumer properties (e.g. `session.timeout.ms`). Keys and values must be non-null.                                              |

---

## 3. Starting Offsets Strategies

Flinkboot supports all native Flink consumption strategies via the `starting-offsets` property:

| Value                | Description                                                     | Required Extra Configuration         |
|:---------------------|:----------------------------------------------------------------|:-------------------------------------|
| `EARLIEST`           | Start consuming from the earliest offset.                       | None                                 |
| `LATEST`             | Start consuming from the latest offset.                         | None                                 |
| `COMMITTED`          | Start from committed offsets. Defaults to latest if none found. | None                                 |
| `COMMITTED_EARLIEST` | Start from committed offsets. Fallback to earliest.             | None                                 |
| `COMMITTED_LATEST`   | Start from committed offsets. Fallback to latest.               | None                                 |
| `TIMESTAMP`          | Start from a specific epoch timestamp.                          | `starting-offsets-timestamp`         |
| `OFFSETS`            | Start from custom offsets specified per partition.              | `starting-offsets-partition-offsets` |

### Timestamp-based Consumption
```yaml
starting-offsets: "TIMESTAMP"
starting-offsets-timestamp: 1689717600000 # Epoch millisecond timestamp
```

### Partition-specific Consumption
```yaml
starting-offsets: "OFFSETS"
starting-offsets-partition-offsets:
  - topic: "users"
    partition: 0
    offset: 12345
  - topic: "users"
    partition: 1
    offset: 23456
```

---

## 4. Java Integration

Embed `KafkaSourceTopicListProperties` inside your application's root configuration class:

### Step 1: Define Root Configuration POJO

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceTopicListProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public final class MyJobConfig {

    @Valid
    @NotNull
    private final KafkaSourceTopicListProperties kafka;

    @JsonCreator
    public MyJobConfig(@JsonProperty("kafka") KafkaSourceTopicListProperties kafka) {
        this.kafka = kafka;
    }

    public KafkaSourceTopicListProperties kafka() { return kafka; }
}
```

### Step 2: Build KafkaSource in Flink Application

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.kafka.api.source.KafkaSourceFactory;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class KafkaConsumerJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        
        // 1. Load configuration from job-configuration.yaml
        MyJobConfig config = boot.configuration(MyJobConfig.class);
        
        // 2. Define your deserialization schema
        KafkaRecordDeserializationSchema<String> schema = 
            KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema());
        
        // 3. Instantiate Flink Kafka Source from properties
        KafkaSource<String> kafkaSource = KafkaSourceFactory.supplyFor(config.kafka(), schema);
        
        // ... build your Flink pipeline
    }
}
```

### Programmatic Customization
If you need to customize Flink's builder (e.g. client ID prefix, custom properties) before building:

```java
KafkaSource<String> customKafkaSource = KafkaSourceFactory.supplyBuilderFor(config.kafka(), schema)
    .setClientIdPrefix("custom-client-id")
    .build();
```

---

## 5. Fail-Fast Validation & Exceptions

- **Nested Bean Validation:** If any property violates constraints (e.g. negative partition or blank topic), a `PropertiesValidationException` is thrown at startup.
- **Invalid Offset Strategy:** If `starting-offsets` is set to `TIMESTAMP` or `OFFSETS` without providing the required timestamp or partition offset list, Flinkboot fails fast with an `InvalidKafkaSourcePropertiesException`.
