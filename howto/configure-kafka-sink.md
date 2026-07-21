# How to Configure a Kafka Sink

Flinkboot provides typed configuration models and a factory to easily initialize Apache Flink's `KafkaSink` from YAML configuration files.

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

You can configure your Kafka Sink using `KafkaSinkConfiguration`:

```yaml
bootstrap-servers:
  - "localhost:9092"
topic: "users-sink"
delivery-guarantee: "EXACTLY_ONCE"
transactional-id-prefix: "my-transactional-prefix"
properties:
  acks: "all"
```

### Configuration Parameters Reference

| Property Key              | Type            | Required | Validation                    | Description                                                                                                                                                                           |
|:--------------------------|:----------------|:---------|:------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `bootstrap-servers`       | List of Strings | **Yes**  | `@NotEmpty`, items `@NotBlank` | Kafka bootstrap broker hosts/ports (e.g. `localhost:9092`).                                                                                                                           |
| `topic`                   | String          | **Yes**  | `@NotBlank`                   | Target Kafka topic to write events to.                                                                                                                                                |
| `delivery-guarantee`      | Enum            | No       | Enum                          | Delivery guarantee. Supported values: `NONE`, `AT_LEAST_ONCE`, `EXACTLY_ONCE`. Defaults to Flink default if omitted.                                                                  |
| `transactional-id-prefix` | String          | No       | String                        | Transactional ID prefix. **Mandatory** only if `delivery-guarantee` is set to `EXACTLY_ONCE`. Must be blank/absent for other delivery guarantees (causes fail-fast crash if present). |
| `properties`              | Map             | No       | Keys/values `@NotBlank`       | Custom Kafka client producer properties (e.g. `acks: all`). Keys and values must be non-null.                                                                                         |

---

## 2. Delivery Guarantees

Flinkboot supports Flink's delivery guarantee strategies via the `delivery-guarantee` property:

| Value            | Description                                                                | Required Extra Configuration |
|:-----------------|:---------------------------------------------------------------------------|:-----------------------------|
| `EXACTLY_ONCE`   | Exactly-once delivery semantics.                                           | `transactional-id-prefix`    |
| `AT_LEAST_ONCE`  | At-least-once delivery semantics.                                          | None                         |
| `NONE`           | Best-effort delivery semantics.                                            | None                         |
| *Omitted (null)* | Let Flink apply its own defaults (leaves configuration builder untouched). | None                         |

### Strict Validation Rules

To prevent misconfigurations at startup, Flinkboot enforces strict validation rules on delivery guarantees:
1. If `delivery-guarantee` is set to `EXACTLY_ONCE`, a non-blank `transactional-id-prefix` **must** be provided.
2. If `delivery-guarantee` is set to `AT_LEAST_ONCE` or `NONE` (or omitted), specifying `transactional-id-prefix` will cause a **fail-fast startup crash**.

---

## 3. Java Integration & Real-world Usage

In a typical production setup, you define a custom `JobConfig` class representing the full application configuration. You embed the `KafkaSinkConfiguration` inside it.

### Step 1: Define the Root Job Configuration Class

```java
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaSinkConfiguration;
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
    private final KafkaSinkConfiguration kafkaSink;

    @JsonCreator
    public JobConfig(
        @JsonProperty("jobName") String jobName,
        @JsonProperty("parallelism") int parallelism,
        @JsonProperty("kafkaSink") KafkaSinkConfiguration kafkaSink
    ) {
        this.jobName = jobName;
        this.parallelism = parallelism;
        this.kafkaSink = kafkaSink;
    }

    public String jobName() { return jobName; }
    public int parallelism() { return parallelism; }
    
    // Returns the nested Kafka configuration
    public KafkaSinkConfiguration kafkaSink() { return kafkaSink; }
}
```

### Step 2: Use in Flink Application

Initialize Flinkboot, retrieve the root `JobConfig`, and build your `KafkaSink` from the nested configuration object:

```java
import io.github.sekelenao.flinkboot.core.api.Flinkboot;
import io.github.sekelenao.flinkboot.kafka.api.sink.KafkaSinkFactory;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class KafkaProducerJob {
    public static void main(String[] args) throws Exception {
        Flinkboot boot = Flinkboot.initialize(args);
        
        // 1. Load the full nested configuration
        JobConfig config = boot.configuration(JobConfig.class);
        
        // 2. Define your serialization schema
        KafkaRecordSerializationSchema<String> schema = 
            KafkaRecordSerializationSchema.builder()
                .setTopic(config.kafkaSink().topic())
                .setValueSerializationSchema(new SimpleStringSchema())
                .build();
        
        // 3. Create the Flink Kafka Sink from the nested config
        KafkaSink<String> kafkaSink = KafkaSinkFactory.supplyFor(config.kafkaSink(), schema);
        
        // ... build and run your Flink pipeline
    }
}
```

### Programmatic Customization

If you need to customize Flink's builder (e.g. custom properties, serialization configuration) before building:

```java
KafkaSink<String> customKafkaSink = KafkaSinkFactory.supplyBuilderFor(config.kafkaSink(), schema)
    .setKafkaProducerConfig(customProps)
    .build();
```

---

## 4. Validation & Exception Handling

### Automatic Nested Validation

The configuration parameters are validated using Jakarta Bean Validation. If any parameter violates constraints (e.g. blank topic, or a null key/value inside the `properties` map), a `ConfigurationValidationException` is thrown at startup.

### Exception Hierarchy

If you specify `transactional-id-prefix` without using `EXACTLY_ONCE` or omit it when `EXACTLY_ONCE` is configured, Flinkboot will throw an:

* **`InvalidKafkaSinkConfigurationException`** (inheriting from `FlinkbootException`).

This ensures self-descriptive error messages and enables you to catch all Flinkboot-related runtime errors under a unified class.
