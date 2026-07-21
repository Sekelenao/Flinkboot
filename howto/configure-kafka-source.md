# How to Configure a Kafka Source

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `KafkaSource` from YAML configuration files.

## Maven Dependency

To use this feature, import the Kafka Flinkboot dependency:

```xml
<dependency>
    <groupId>io.github.sekelenao</groupId>
    <artifactId>flinkboot-kafka</artifactId>
</dependency>
```

---

## 1. YAML Configuration Structure

You can configure your Kafka Source using either a static list of topics or a topic pattern regex.

### Option A: Static List of Topics (`KafkaSourceTopicListConfiguration`)

```yaml
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

### Option B: Topic Pattern Regex (`KafkaSourceTopicPatternConfiguration`)

```yaml
bootstrap-servers:
  - "localhost:9092"
group-id: "my-consumer-group"
topic-pattern: "^my-topic-.*$"
starting-offsets: "LATEST"
```

### Configuration Parameters Reference

| Property Key                         | Type            | Required                    | Validation                        | Description                                                                                                                                         |
|:-------------------------------------|:----------------|:----------------------------|:----------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------|
| `bootstrap-servers`                  | List of Strings | **Yes**                     | `@NotEmpty`, items `@NotBlank`     | Kafka bootstrap broker hosts/ports (e.g. `localhost:9092`).                                                                                         |
| `group-id`                           | String          | **Yes**                     | `@NotBlank`                       | Consumer group ID.                                                                                                                                  |
| `topics`                             | List of Strings | **Yes** (Only for Option A) | `@NotEmpty`, items `@NotBlank`     | Static list of topics to subscribe to.                                                                                                              |
| `topic-pattern`                      | String          | **Yes** (Only for Option B) | `@NotBlank`                       | Regex pattern to match topic subscriptions.                                                                                                         |
| `starting-offsets`                   | Enum            | **Yes**                     | `@NotNull` Enum                   | Strategy to start consuming. Supported values: `EARLIEST`, `LATEST`, `COMMITTED`, `COMMITTED_EARLIEST`, `COMMITTED_LATEST`, `TIMESTAMP`, `OFFSETS`. |
| `starting-offsets-timestamp`         | Long            | No                          | `@PositiveOrZero`                 | Timestamp in epoch milliseconds. **Mandatory** only if `starting-offsets` is set to `TIMESTAMP` (otherwise ignored). Must be positive or zero.      |
| `starting-offsets-partition-offsets` | List            | No                          | `@Valid` list items               | Specific partition offset offsets mapping. **Mandatory** only if `starting-offsets` is set to `OFFSETS` (otherwise ignored).                        |
| `properties`                         | Map             | No                          | Keys/values `@NotBlank`           | Custom Kafka client consumer properties (e.g. `session.timeout.ms`). Keys and values must be non-null.                                              |

---

## 2. Advanced Starting Offsets Strategies

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

### Configuring Timestamp-based Starting Offsets

```yaml
starting-offsets: "TIMESTAMP"
starting-offsets-timestamp: 1689717600000 # Epoch millisecond timestamp
```

### Configuring Partition-specific Starting Offsets

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

## 3. Java Integration & Real-world Usage

In a typical production setup, you define a custom `JobConfig` class representing the full application configuration. You embed the `KafkaSourceTopicListConfiguration` inside it.

### Step 1: Define the Root Job Configuration Class

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.kafka.api.configuration.source.KafkaSourceTopicListConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class JobConfig {

    @NotBlank
    private final String jobName;

    @Min(1)
    private final int parallelism;

    @Valid
    @NotNull
    private final KafkaSourceTopicListConfiguration kafka;

    @JsonCreator
    public JobConfig(
        @JsonProperty("jobName") String jobName,
        @JsonProperty("parallelism") int parallelism,
        @JsonProperty("kafka") KafkaSourceTopicListConfiguration kafka
    ) {
        this.jobName = jobName;
        this.parallelism = parallelism;
        this.kafka = kafka;
    }

    public String jobName() { return jobName; }
    public int parallelism() { return parallelism; }
    
    // Returns the nested Kafka configuration
    public KafkaSourceTopicListConfiguration kafka() { return kafka; }
}
```

### Step 2: Use in Flink Application

Initialize Flinkboot, retrieve the root `JobConfig`, and build your `KafkaSource` from the nested configuration object:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.kafka.api.source.KafkaSourceFactory;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class KafkaConsumerJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        
        // 1. Load the full nested configuration
        JobConfig config = boot.configuration(JobConfig.class);
        
        // 2. Define your deserialization schema
        KafkaRecordDeserializationSchema<String> schema = 
            KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema());
        
        // 3. Create the Flink Kafka Source from the nested config
        KafkaSource<String> kafkaSource = KafkaSourceFactory.supplyFor(config.kafka(), schema);
        
        // ... build and run your Flink pipeline
    }
}
```

### Programmatic Customization

If you need to customize Flink's builder (e.g. client ID prefix, custom properties) before building:

```java
KafkaSource<String> customKafkaSource = KafkaSourceFactory.supplyBuilderFor(config.kafka(), schema)
    .setClientIdPrefix("custom-client-id")
    .setProperty("kafka.custom.property", "value")
    .build();
```


---

## 4. Validation & Exception Handling

### Automatic Nested Validation

The configuration parameters are validated using Jakarta Bean Validation. If any parameter violates constraints (e.g. a negative partition or blank topic in the partition offsets list), a `ConfigurationValidationException` is thrown at startup.

### Exception Hierarchy

If you configure `starting-offsets` to `TIMESTAMP` or `OFFSETS` but fail to specify the required timestamp or partition offsets list, Flinkboot will throw an:

* **`InvalidKafkaSourceConfigurationException`** (inheriting from `FlinkbootException`).

This ensures self-descriptive error messages and enables you to catch all Flinkboot-related runtime errors under a unified class.
