# How to Configure a Kafka Source

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `KafkaSource` from YAML configuration files.

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

---

## 2. Advanced Starting Offsets Strategies

Flinkboot supports all native Flink consumption strategies via the `starting-offsets` property:

| Value | Description | Required Extra Configuration |
| :--- | :--- | :--- |
| `EARLIEST` | Start consuming from the earliest offset. | None |
| `LATEST` | Start consuming from the latest offset. | None |
| `COMMITTED` | Start from committed offsets. Defaults to latest if none found. | None |
| `COMMITTED_EARLIEST` | Start from committed offsets. Fallback to earliest. | None |
| `COMMITTED_LATEST` | Start from committed offsets. Fallback to latest. | None |
| `TIMESTAMP` | Start from a specific epoch timestamp. | `starting-offsets-timestamp` |
| `OFFSETS` | Start from custom offsets specified per partition. | `starting-offsets-partition-offsets` |

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

## 3. Java Integration

To use the Kafka Source in your job, parse the configuration using Flinkboot and construct it using `KafkaSourceFactory`.

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSourceTopicListConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.source.KafkaSourceFactory;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class KafkaConsumerJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        
        // 1. Load the configuration
        KafkaSourceTopicListConfiguration config = boot.configuration(KafkaSourceTopicListConfiguration.class);
        
        // 2. Define your deserialization schema
        KafkaRecordDeserializationSchema<String> schema = KafkaRecordDeserializationSchema.valueOnly(new SimpleStringSchema());
        
        // 3. Create the Flink Kafka Source
        KafkaSource<String> kafkaSource = KafkaSourceFactory.supplyFor(config, schema);
        
        // ... build your Flink pipeline
    }
}
```

### Programmatic Customization

If you need to add custom properties or configure features not mapped in the YAML file (e.g. client ID prefix), you can retrieve the builder instead of the constructed source:

```java
KafkaSource<String> customKafkaSource = KafkaSourceFactory.supplyBuilderFor(config, schema)
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
